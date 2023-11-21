package edu.jit.nsi.iot_ms.serviceimpl.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.controller.UsrOptController;
import edu.jit.nsi.iot_ms.domain.*;
import edu.jit.nsi.iot_ms.mapper.EnvirDataDAO;
import edu.jit.nsi.iot_ms.mapper.EquipmentActionDAO;
import edu.jit.nsi.iot_ms.mapper.PlatStatDailyDAO;
import edu.jit.nsi.iot_ms.mapper.SensorPhyDAO;
import edu.jit.nsi.iot_ms.transport.msg.*;
import edu.jit.nsi.iot_ms.transport.msg.EnviHisAggrRsp.SNTV;
import edu.jit.nsi.iot_ms.config.InfluxdbDaoImpl;
import edu.jit.nsi.iot_ms.serviceimpl.TermRspHandler;
import edu.jit.nsi.iot_ms.serviceimpl.custom.CellServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.SensorServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.session.SensorParamSession;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.*;
import edu.jit.nsi.iot_ms.transport.tcp.EquipNewRsp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
public class AppServiceImpl {
    @Autowired
    SessionManager sessMngr;

    @Autowired
    EnvirDataDAO envdao;

    @Autowired
    SensorPhyDAO phydao;

    @Autowired
    EquipmentActionDAO epactdao;

    @Autowired
    PlatStatDailyDAO dailyplatdao;

    @Autowired
    JitServiceImpl minaService;

    @Autowired
    LoraServiceImpl loraService;

    @Autowired
    EquipServiceImpl equipService;

    @Autowired
    CellServiceImpl cellService;

    @Autowired
    TerminalServiceImpl termService;

    @Autowired
    SensorServiceImpl senService;

    @Autowired
    InfluxdbDaoImpl influxDaoImpl;

    @Resource
    SensorCmdCfg sensorCmdCfg;

    @Autowired
    UsrOptController optController;

    boolean ctrloverfg = false;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 针对某个生产单元实时查询
     * @param cellid  生产单元id
     */
    public List<EnviNewSmryRsp> onCellEnvSmryQuery(int cellid, boolean allenv){
        List<EnviNewSmryRsp> envlist = new ArrayList<EnviNewSmryRsp>();
        //生产单元关联的所有传感器
        List<SensorParamDO> senlist = senService.cellParamsDO(cellid, allenv);
        if(senlist==null){
            return null;
        }
        for (SensorParamDO param : senlist) {
            SensorParamSession session = sessMngr.getParamData(param.getId());
            if(session!=null){
                envlist.add(new EnviNewSmryRsp(session.getParamid(), session.getValue()));
            }
        }
        if(envlist.size()>0)
            return envlist;
        else
            return null;
    }


    /**
     * 某个生产单元下的设备状态查询
     * @param cellId  生产单元id
     */
    public List<EquipNewRsp> onCellEqpQuery(int cellId){
        List<EquipNewRsp> equRsplist = new ArrayList<EquipNewRsp>();
        List<EquipDO> equlist = equipService.equip_list(cellId);//获取所有的设备信息
        for(EquipDO epdo:equlist){
            int epid = epdo.getId();
            EquipNewRsp equRsp = sessMngr.getEquipStatus(epid);//从缓存中获取设备状态
            if(equRsp!=null){
                //从数据库中读取最新的设备状态
                List<EquipActionDO> acts = epactdao.selectList(new EntityWrapper<EquipActionDO>().eq("equipid", epid).orderBy("id",false).last("LIMIT 1"));

                if(acts!=null && acts.size()!=0){
                    EquipActionDO act = acts.get(0);
                    if(act.getOnofflg()!=equRsp.getStatus()){
                        log.error("current equip status not match, db_stat:{}, session_sta:{}",act.getOnofflg(),equRsp.getStatus() );
                    }
                    equRsp.setTime(act.getTime());
                }
                equRsplist.add(equRsp);//添加设备状态
            }
        }
        return equRsplist;//返回所有设备的状态列表
    }



    /**
     * 塘口某参数设备状态查询
     * @param cellId  塘口id
     * @param param  控制参数
     */
    public List<EquipNewRsp> paramEqpQuery(int cellId, String param){
        List<EquipNewRsp> equRsplist = new ArrayList<EquipNewRsp>();
        List<EquipDO>equlist =equipService.cellParamEqp_list(cellId, param);
        for(EquipDO epdo:equlist) {
            int epid = epdo.getId();
            EquipNewRsp equRsp = sessMngr.getEquipStatus(epid);
            if(equRsp!=null){
                List<EquipActionDO> acts = epactdao.selectList(new EntityWrapper<EquipActionDO>().eq("equipid", epid).orderBy("id",false).last("LIMIT 1"));
                if(acts!=null && acts.size()!=0){
                    EquipActionDO act = acts.get(0);
                    if(act.getOnofflg()!=equRsp.getStatus()){
                        log.error("current equip status not match, db_stat:{}, session_sta:{}",act.getOnofflg(),equRsp.getStatus() );
                    }
                    equRsp.setTime(act.getTime());
                }
                equRsplist.add(equRsp);
            }
        }
        return equRsplist;
    }

    /**
     * 获取某用户的所有终端（及传感器）状态
     * @param usrname  用户名
     */
    public List<SessionManager.TermSta> getUsrTermSta(String usrname){
        return sessMngr.getTermSta(usrname,true);
    }

    /**
     * 获取系统中每个终端的状态
     */
    public List<SessionManager.TermSta> getAllTermSta(){
        return sessMngr.getTermSta(null,false);
    }


    /**
     * 获取系统中termid终端的状态
     */
    public TermStatus getNewTermSta(int termid){
        return sessMngr.getNewTermStat(termid);
    }

    /**
     * 获取termid按日期统计收发包、电量等数据
     */
    public List<DailyTermStatus> getDailyTermSta(int termid,String start, String end){
        return dailyplatdao.getDailyTermSta(termid, start, end);
    }


    /**
     * 按照termid统计一段时间内收发包、电量等数据
     */
    public List<HisTermStaAggr> getHisTermStaAggr(String termids,String start, String end){
        List<String> termlist = new ArrayList<>();
        String[] cids = termids.split(",");
        for(String id: cids){
            termlist.add(id);
        }
        return dailyplatdao.getHisTermStaAggr(termlist, start, end);
    }

    /**
     * 展示在离线数量与列表
     */
    public TermStaLst getTermStaNumLst(String termids){
        TermStaLst tsLst = new TermStaLst();
        String[] cids = termids.split(",");
        for(String tid: cids){
            int termid = Integer.parseInt(tid);
            if(sessMngr.isTermExist(termid)) {
                tsLst.addOnTerm(termid);
            }else{
                tsLst.addOffTerm(termid);
            }
        }

        return tsLst;
    }


    /**
     * 某个生产单元下的设备状态日志查询
     * @param cellid  生产单元id
     * @param start  开始时间
     * @param end  结束时间
     */
    public Collection<EqpLogRsp> onCellEqpLog(int cellid, String start, String end){

        Map<Integer,  EqpLogRsp> eqpLogMap = new HashMap<>();
        Map<Integer,  EqpStaTm> eqpSTMap = new HashMap<>();

        List<EquipActionDO> eqlogs = epactdao.equipActLog(cellid,start,end);
        for(EquipActionDO act : eqlogs){
            int epid = act.getEquipid();
            if(!eqpLogMap.containsKey(epid)){
                boolean cursta= (act.getOnofflg()==1)?true:false;
                long curtm=act.getTime().getTime()/1000;
                long pretm=curtm;
                try {
                    pretm = sdf.parse(start).getTime()/1000;
                }catch (Exception e){
                    log.error("onCellEqpLog start_time:{} illegal",start);
                    return null;
                }
                eqpSTMap.put(epid, new EqpStaTm(cursta,curtm,!cursta,pretm));
                eqpLogMap.put(epid, new EqpLogRsp(epid, equipService.equip_info(epid).getDefname()));
            }
            EqpLogRsp eqp = eqpLogMap.get(epid);
            EqpStaTm statm = eqpSTMap.get(epid);
            statm.setCursta((act.getOnofflg()==1)?true:false);
            statm.setCurtm(act.getTime().getTime()/1000);

            int last = (int)(statm.getCurtm()-statm.getPretm());
            //增加对应开关时长
            if(!statm.isPresta()&&statm.isCursta()) { //0->1
                eqp.incrOffTm(last);
            }else if(statm.isPresta()&&!statm.isCursta()) { //1->0
                eqp.incrOnTm(last);
            }else if(statm.isPresta()&&statm.isCursta()){  //1->1
                eqp.incrOnTm(last);
            }else{ //0->0
                eqp.incrOffTm(last);
            }

            //追加一条log记录
            eqp.getOptlist().add(act.getOnofflg());
            eqp.getTimelist().add(statm.getCurtm());

            //更新状态
            statm.setPresta(statm.isCursta());
            statm.setPretm(statm.getCurtm());
        }

        //end时间结束
        long endtm=0;
        try {
            endtm = sdf.parse(end).getTime()/1000;
        }catch (Exception e){
            //结束时间不对
            log.error("onCellEqpLog end_time:{} illegal",end);
            return null;
        }

        for(EqpLogRsp eqp : eqpLogMap.values()){
            EqpStaTm st = eqpSTMap.get(eqp.getEqpid());
            int lastend = (int)(endtm-st.getCurtm());
            if(st.isCursta()){
                eqp.incrOnTm(lastend);
            }else {
                eqp.incrOffTm(lastend);
            }
        }
        return eqpLogMap.values();
    }

    /**
     * 针对某个生产单元历史查询
     * @param cellid  生产单元id
     * @param start  开始时间
     * @param end  结束时间
     */
    public List<EnviHisAggrRsp> onCellHisSmryQuery(int cellid, boolean allenvfg, String start, String end){
        List<ParamID> paramIDs= phydao.paramsInCell(cellid);
        List<ParamID> paramlist;
        if(!allenvfg){
            paramlist = new ArrayList<>();
            for(ParamID param:paramIDs){
                if(sensorCmdCfg.isEnvToUsr(param.getParam())){
                    paramlist.add(param);
                }
            }
        }else{
            paramlist = paramIDs;
        }
        return aggrByParam(paramlist, start, end);
    }

    /**
     * 针对某个环境参数的历史查询
     * @param paramid  生产单元id
     * @param start  开始时间
     * @param end  结束时间
     */
    public SNTV onParamHisQuery(int paramid, String start, String end){
//        List<EnvirDataDO> envirlist= envdao.selectList(new EntityWrapper<EnvirDataDO>()
//               .eq("snpid", paramid).between("time", start, end));
//        SNTV sntv = new SNTV(paramid);
//        for(EnvirDataDO env : envirlist) {
//            sntv.getTimes().add(env.getTime().getTime()/1000);
//            sntv.getValues().add(env.getValue());
//        }
//        return sntv;
       EnviHisAggrRsp.SNTV sntv = new EnviHisAggrRsp.SNTV(paramid);
       constructAggrRspFromInflux(paramid, sntv, start, end);
       return sntv;
    }

    /**
     * 针对某个终端具体参数的历史查询
     * @param termid  终端id
     * @param type    类型type
     * @param start  开始时间
     * @param end  结束时间
     */
    public SNTV hisByTermType(int termid, String type, String start, String end){
        int paramid=-1;
        EnviHisAggrRsp.SNTV sntv;
        List<Integer> paidList = phydao.paramsInTermType(termid, type);
        if(paidList!=null && paidList.size()>0){
            paramid=paidList.get(0);
            sntv = new EnviHisAggrRsp.SNTV(paramid);
            constructAggrRspFromInflux(paramid, sntv, start, end);
        }else{
            sntv = new EnviHisAggrRsp.SNTV(paramid);
            return sntv;
        }
        return sntv;
    }

    /**
     * 获取按天气象参数统计数据
     * @param termid   终端ID
     * @param start    开始时间
     * @param end      结束时间
     */
    public List<WhrStnIlluIntgl> integalIllu(int termid, String start, String end){
        List<WhrStnIlluIntgl> illuIntlst = new ArrayList<>();
        influxDaoImpl.illuIntegral(termid, start, end, illuIntlst);
        return illuIntlst;
    }


    /**
     * 获取按天气象参数统计数据
     * @param termid   终端ID
     * @param type     参数类型
     * @param start    开始时间
     * @param end      结束时间
     */
    public List<WhrStnAnaly> getDailyParamAggr(int termid, String type, String start, String end){
        List<WhrStnAnaly> whrStnAnaLst = new ArrayList<>();
        influxDaoImpl.queryAnaly(termid, type, start, end,whrStnAnaLst);
        return whrStnAnaLst;
    }


    /**
     * 通过光照=0的时间列表获得每天的日出日落和时长
     * @param termid   终端ID
     * @param start    开始时间
     * @param end      结束时间
     */
    public List<WhrStnSunRiseSetAnaly> analySunTimeFromSeq(int termid, String start, String end){
        List<String> darktimesLst = new ArrayList<>();
        influxDaoImpl.querySuntime(termid,start,end,darktimesLst);
        List<WhrStnSunRiseSetAnaly> sunRiseSet = new ArrayList<>();
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss+08:00");
        String curstr=null;
        String prestr=null;
        Date curdate=null; //当前时间
        Date predate=null; //上次时间
        Date noon = null;
        Date midnight = null;
        try{
            midnight=timeFormat.parse("00:00:00+08:00");
            noon = timeFormat.parse("12:00:00+08:00");
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        for(String curtm:darktimesLst) {
            String[] tmp;
            //将yyyy-MM-dd'T'HH:mm:ss拆分为日期和时间
            tmp = curtm.split("T");
            if (tmp.length != 2) {
                continue;//忽略此条时间
            }

            curstr = tmp[1];
            try {
                curdate = timeFormat.parse(tmp[1]);
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }

            if (predate == null) {
                if (curdate.after(noon)) {
                    WhrStnSunRiseSetAnaly singleday = new WhrStnSunRiseSetAnaly(tmp[0]);
                    singleday.setSet(curstr);
                    singleday.setSet_5min((int) (curdate.getTime() - midnight.getTime()) / (5 * 60 * 1000));
                    sunRiseSet.add(singleday);
                }
            } else {
                float durhour = (float) ((curdate.getTime() - predate.getTime()) * 1.0 / (60 * 60 * 1000));
                if (durhour > 8.0) {
                    WhrStnSunRiseSetAnaly singleday = new WhrStnSunRiseSetAnaly(tmp[0]);
                    singleday.setRise(prestr);
                    singleday.setSet(curstr);
                    singleday.setRise_5min((int) (predate.getTime() - midnight.getTime()) / (5 * 60 * 1000));
                    singleday.setSet_5min((int) (curdate.getTime() - midnight.getTime()) / (5 * 60 * 1000));
                    singleday.setSuntime(durhour);
                    sunRiseSet.add(singleday);
                }
            }

            predate = curdate;
            prestr = curstr;
        }
        return sunRiseSet;
    }

    /**
     *终端电池电量历史查询
     * 针对矽递终端、农芯EP400终端、易通水质终端
     * @param termid  生产单元id
     * @param start  开始时间
     * @param end  结束时间
     */
    public TermVoltHis ontermVoltHisQuery(int termid, String start, String end){
        TermVoltHis termVoltHis = new TermVoltHis(termid);

        TermDO tdo = termService.getTermById(termid);
        int termTpInt = tdo.getType();
        String tmtp;
        if(termTpInt == 2){
            tmtp = "btyPer";
        }else if(termTpInt == 3||termTpInt == 4){
            tmtp = "volt";
        }else{
            return null;
        }

        List<EnvirDataDO> envirlist= envdao.selectList(new EntityWrapper<EnvirDataDO>()
                .eq("type", tmtp).eq("termid", termid).between("time", start, end));

        for(EnvirDataDO envir:envirlist){
            termVoltHis.addItem(envir.getTime().getTime()/1000, envir.getValue());
        }
        return termVoltHis;
    }


    /**
     * 环境参数曲线对比
     * @param cell_ids  生产单元集合
     * @param pa_types  采集参数集合
     * @param start  开始时间
     * @param end  结束时间
     */
    public List<EnviHisAggrRsp> onCellHisAggrQuery(String cell_ids, String pa_types, String start, String end) {
        List<Integer> cells = new ArrayList<>();
        List<String> ptypes = new ArrayList<>();
        String[] cids = cell_ids.split(",");
        String[] params = pa_types.split(",");
        for(String id: cids){
            cells.add(Integer.parseInt(id));
        }
        for(String pa:params){
            ptypes.add(pa);
        }

        /*根据生产单元cell下的采集参数param*/
        List<ParamID> paramIDs= phydao.phySnByCellType(cells, ptypes);
        return aggrByParam(paramIDs, start, end);
    }

    /**
     * 通过param和snpid按二级分类来整理历史数据
     * @param paramIDs  param和snpid的集合
     */
    private List<EnviHisAggrRsp> aggrByParam(List<ParamID> paramIDs,String start, String end){
        List<EnviHisAggrRsp> aggrRsps= new ArrayList<>();
        Map<String, List<Integer>> snpidByType = new HashMap<>(); //Map<type, paramIdList>
        Map<Integer, Integer> paramprdMap = new HashMap<>();
//        Map<Integer, EnviHisAggrRsp.SNTV> paramEnvMap = new HashMap<>(); //Map<paramId,SNTV>

        for(ParamID paid : paramIDs){
            String pa = paid.getParam();
            int pid = paid.getId();
            if(!snpidByType.containsKey(pa)){
                snpidByType.put(pa, new ArrayList<>());
            }
            snpidByType.get(pa).add(pid);
            paramprdMap.put(pid, paid.getDatacycle());
//            paramEnvMap.put(pid,new EnviHisAggrRsp.SNTV(pid));
        }

        for(String ptype : snpidByType.keySet()){
            EnviHisAggrRsp typeAggr = new EnviHisAggrRsp(ptype);
            //按照type分组添加下面的snpid的历史记录
            for(Integer snpid:snpidByType.get(ptype)){
                int cycle = 5;
                if(paramprdMap.get(snpid)!=null)
                    cycle = paramprdMap.get(snpid);
//                EnviHisAggrRsp.SNTV sntv = constructAggrRsp(snpid, start, end, cycle);
//                EnviHisAggrRsp.SNTV sntv = constructAggrRsp2(snpid, start, end);
                EnviHisAggrRsp.SNTV sntv = new EnviHisAggrRsp.SNTV(snpid);
                constructAggrRspFromInflux(snpid, sntv, start, end); //20220901-from influxdb
                typeAggr.getSntvs().add(sntv);
            }
            aggrRsps.add(typeAggr);
        }
        return  aggrRsps;
    }

    /**
     * 生成param参数的历史数据，根据上报间隔判断添加空值null
     * @param paramid   参数ID
     * @param start     开始时间
     * @param end       结束时间
     */
    private EnviHisAggrRsp.SNTV constructAggrRsp(int paramid, String start, String end, int cycle){
        EnviHisAggrRsp.SNTV sntv = new EnviHisAggrRsp.SNTV(paramid);
        List<EnvirDataDO> envirlist= envdao.selectList(new EntityWrapper<EnvirDataDO>()
                .eq("snpid", paramid).between("time", start, end));

        long start_utc=0, end_utc=0;
        try {
            start_utc = sdf.parse(start).getTime()/1000;
            end_utc = sdf.parse(start).getTime()/1000;
        }catch (Exception e){
            log.error("onCellEqpLog start_time:{} illegal",start);
            return null;
        }

        int num=(int)(end_utc-start_utc)/(cycle);
        //丢失超过5%的数据，需要补null
        if((float)envirlist.size()/num < 0.95){

        }

        //判断start至第一个point是否需要补充
        long last_time = start_utc;
        for(EnvirDataDO envir:envirlist){
            long point_time = envir.getTime().getTime()/1000;
            float point_value = envir.getValue();
            //添加缺失的数据
            int absent_prd = (int)((point_time-last_time)/(cycle));
            //考虑超时延迟系数，可能会存在两个连续上报的时间间隔是cycle*1.01
            if(absent_prd>=2){
                for(int i=0; i<absent_prd; i++) {
                    sntv.getTimes().add(last_time + i);
                    sntv.getValues().add(null);
                }
            }

            //添加当前的数据
            sntv.getTimes().add(point_time);
            sntv.getValues().add(point_value);
            //更新时间
            last_time = point_time;
        }

        //补充最后缺失的数据
        int lst_absent_prd = (int)(end_utc-last_time)/(cycle);
        if(lst_absent_prd>2){
            for(int j=0; j<lst_absent_prd; j++) {
                sntv.getTimes().add(last_time + j);
                sntv.getValues().add(null);
            }
        }
        return sntv;
    }

    /**
     * 生成param参数的历史数据，不判断null
     * @param paramid   参数ID
     * @param start     开始时间
     * @param end       结束时间
     */
    private EnviHisAggrRsp.SNTV constructAggrRsp2(int paramid, String start, String end){
        EnviHisAggrRsp.SNTV sntv = new EnviHisAggrRsp.SNTV(paramid);
        List<EnvirDataDO> envirlist= envdao.selectList(new EntityWrapper<EnvirDataDO>()
                .eq("snpid", paramid).between("time", start, end));

        for(EnvirDataDO envir:envirlist){
            sntv.getTimes().add(envir.getTime().getTime()/1000);
            sntv.getValues().add(envir.getValue());
        }
        return sntv;
    }

    /**
     * 生成param参数的历史数据，不判断null
     * @param paramid   参数ID
     * @param start     开始时间
     * @param end       结束时间
     */
    private void constructAggrRspFromInflux(int paramid, EnviHisAggrRsp.SNTV sntv, String start, String end){
        influxDaoImpl.querySNTV(paramid, start, end, sntv.getTimes(), sntv.getValues());
    }

    /**
     * 实时控制
     * @param eqipid   设备ID
     * @param onfg 1:on, 0:off
     */
    public void onCntrl(int httpsessionid, int eqipid, int onfg){
        EquipDO equipData = equipService.equip_info2(eqipid);//查找设备的详细信息
        TermDO termDO= termService.getTermById(equipData.getTermid());//终端详细信息
        TermRspHandler handler = new TermRspHandler(){
            //控制成功要执行回调函数
            public void execute() {
                optController.controlReturn(eqipid);
            }
            //控制超时要执行回调函数
            public void timeout(){
                optController.controlTimeout(eqipid);
            }
        };
        if(termDO.getType()==1){
            //通过minaservice发送control请求，并注册回调函数
            minaService.sendCntl(0,  equipData.getTermid(),
                    equipData.getAddr(), equipData.getRoad(), onfg, handler);
        }else if(termDO.getType()==5){
            loraService.downLinkCtrl(0, termDO.getDeveui().toLowerCase(),
                    equipData.getAddr(), equipData.getRoad(), onfg, handler);
        }
    }

    public void rspCtrl(){
        ctrloverfg = true;
    }

    /**
     * 终端参数数据导出
     * @param termid   终端ID
     * @param start 开始时间
     * @param end   终止时间
     */
    public Map<String, Map<String, Float>> exportTermParam(int termid, String paramlst, String start, String end){
        List<String> ptypes = new ArrayList<>();
        Map<String, Map<String, Float>> exportRes = new HashMap<>();
        String[] params = paramlst.split(",");
        for(String pa:params){
            ptypes.add(pa);
        }
        List<EnviParamExp> envexport = envdao.exportParam(termid, start,end, ptypes);
        for(EnviParamExp env : envexport){
            String curtm = env.getTime();
            if(!exportRes.containsKey(curtm)){
                exportRes.put(curtm, new HashMap<>());
            }
            exportRes.get(curtm).put(env.getType(), env.getValue());
        }
        return exportRes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamID{
        public String param;
        public int id;
        public int datacycle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EqpStaTm{
        public boolean cursta ;  //遍历过程中当前状态
        public long curtm;  //遍历过程中当前UTC时间
        public boolean presta;  //遍历过程中前一个状态
        public long pretm;  //遍历过程中前一个UTC时间
    }

    @Data
    public static class TermVoltHis{
        public int termid ;  //遍历过程中当前状态
        public List<Long> times;  //遍历过程中当前UTC时间
        public List<Float> values;  //遍历过程中当前UTC时间

        public TermVoltHis(int tid){
            termid = tid;
            times = new ArrayList<>();
            values = new ArrayList<>();
        }

        public void addItem(long t, float va){
            times.add(t);
            values.add(va);
        }
    }

}
