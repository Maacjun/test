package edu.jit.nsi.iot_ms.session;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.SensorCmd;
import edu.jit.nsi.iot_ms.config.SensorValue;
import edu.jit.nsi.iot_ms.domain.*;
import edu.jit.nsi.iot_ms.mapper.CellDAO;
import edu.jit.nsi.iot_ms.mapper.EnvirDataDAO;
import edu.jit.nsi.iot_ms.mapper.EquipmentActionDAO;
import edu.jit.nsi.iot_ms.mapper.PlatStatDAO;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.SensorServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.transport.ReportData;
import edu.jit.nsi.iot_ms.transport.msg.TermStatus;
import edu.jit.nsi.iot_ms.transport.tcp.EquipNewRsp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SessionManager {
    private final static int SCANSEC = 300;

    @Autowired
    TerminalServiceImpl termService;

    @Autowired
    SensorServiceImpl senService;

    @Autowired
    EquipServiceImpl equipService;

    @Autowired
    EnvirDataDAO envdao;

    @Autowired
    EquipmentActionDAO equipmentActionDAO;

    @Autowired
    PlatStatDAO platDAO;

    @Autowired
    CellDAO cellDAO;

    //mina lora nb通用Session Map
    private ConcurrentHashMap<Integer, TermSession> termMap = new ConcurrentHashMap<Integer, TermSession>();
    private Map<Integer, SensorParamSession> paramMap = new HashMap<>();
    private Map<Integer, EquipSession> equipMap = new HashMap<Integer, EquipSession>();
    private int cycnt = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @Description 新增一条 session
     **/
    public synchronized void putOneSess(int termType, int termid, int datacyc, int toplat,  IoSession session, String deveui) {
        //增加term session
        TermSession termSession;
        switch (termType) {
            case 1:
                termSession = new TermSession(termid, termType, datacyc,toplat, session);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                termSession = new TermSession(termid, termType, datacyc,toplat, deveui);
                break;
            default:
                return;
        }
        termMap.put(termid, termSession);
        //增加sensor jit.edu.nsi.config session
        List<SensorPhyDO> sensorCfgList = senService.termPhySensors(termid);
        for (SensorPhyDO sencfg : sensorCfgList) {
            if (sencfg.getProduct().toLowerCase().contains("relay") || sencfg.getProduct().toLowerCase().contains("dma"))
                continue;
            List<SensorParamDO> snparams = senService.sensorParams(sencfg.getId());
            for (SensorParamDO param : snparams) {
                termSession.getSensorparams().add(param.getId());
                paramMap.put(param.getId(), new SensorParamSession(sencfg, param));
            }
        }
        //增加equipment jit.edu.nsi.config session
        List<EquipDO> equipList = equipService.equipsInTerm(termid);
        for (EquipDO equipDO : equipList) {
            termSession.getEquiplst().add(equipDO.getId());
            equipMap.put(equipDO.getId(), new EquipSession(equipDO));
        }
    }

    /**
     * @Description 保存AG21终端物理信息
     **/
    public void ag21TermPhyInfo(int termid, List<SensorCmd> sensors) {
        for (SensorCmd scmd : sensors) {
            int reg = (Integer) scmd.getReg().get(0);
            int len = scmd.getLen();
            String type = scmd.getType();
            List<SensorValue> rspvalst = scmd.getRspvalue();

            for (Integer ad : (List<Integer>) scmd.getAddr()) {
                termMap.get(termid).addPhysnrlst(new PhySnrInfo(ad, type, reg, len, rspvalst));
            }

        }
    }

    public void updateEquipCtl(int termid, int addr, int road, int onoff, int ctlmode) {
        Date now = new Date();

        EquipDO equip = equipService.getEquipbyTermAddr(termid, addr, road);
        if (equip == null) {
            log.error("the eqip termid{}, addr{}, road{} not exist in db!", termid, addr, road);
            return;
        }
        EquipSession eqSession = equipMap.get(equip.getId());
        if (eqSession == null) {
            log.error("the eqid{} not exist in session!", equip.getId());
            return;
        }
        if (eqSession.getStatus() != onoff) {
            equipmentActionDAO.insert(new EquipActionDO(eqSession.getId(), (byte) onoff, (byte) ctlmode, now));
        }
        eqSession.setStatus(onoff);
        eqSession.setActive(true);
        eqSession.setTime(now);
    }

    //接到上报的消息后更新Sensor Session中的值
    public void updateSessByReport(int termid, List<ReportData> reports, int ctlmode) {
        Date now = new Date();
        TermSession termsess = termMap.get(termid);
        termsess.setTime(now);
        List<Integer> senCfgList = termsess.getSensorparams();
        List<Integer> equipList = termsess.getEquiplst();
        for (ReportData report : reports) {
            if (!report.getType().toLowerCase().contains("relay") && !report.getType().toLowerCase().contains("dma")) {
                //处理上报的传感器信息
                for (int sid : senCfgList) {
                    SensorParamSession paramSession = paramMap.get(sid);
                    if (paramSession == null) {
                        log.error("the sensorid{} not exist in session!", sid);
                    } else if (paramSession.getAddr() == report.getAddr() && paramSession.getReg() == report.getReg()) {
                        updateSensor(paramSession, report, now);
                    }
                }
                //设置电量
                if (report.getType().toLowerCase().contains("volt")){
                    termsess.setVolt(report.getValue());
                }
            } else {
                //处理上报的继电器信息
                for (int eqid : equipList) {
                    EquipSession eqSession = equipMap.get(eqid);
                    if (eqSession == null) {
                        log.error("the eqid{} not exist in session!", eqid);
                    } else if (eqSession.getAddr() == report.getAddr()) {
                        updateEquipReport(eqSession, report, ctlmode, now);
                    }
                }
            }
        }
    }




    //根据传感器参数id获取环境参数
    public SensorParamSession getParamData(int spid) {
        SensorParamSession sensess = paramMap.get(spid);
        if (sensess == null) {
            return null;
        }

//        int interval_min = (int)(new Date().getTime()-sensess.getTime().getTime())/1000/60;
        int reportperoid = 0;
        //获取Sensor对应的Term,找到term的更新时间
        TermSession termSession = termMap.get(sensess.getTermid());
        if (termSession == null) {
            log.error("the termid{} match sensorid{} not exist!", sensess.getTermid(), spid);
            return null;
        }
        reportperoid = getTermPeriod(termSession.getTermid());

        //2倍的上报周期
//        if(interval_min > reportperoid*2){
//            return null;
//        }

        return sensess;
    }

    //根据ID获取设备的状态
    public EquipNewRsp getEquipStatus(int equipid) {
        EquipSession equipSession = equipMap.get(equipid);
        if (equipSession == null) {
            log.error("equipid={} in equipmap(size={}) not exist, maybe not recv report msg!", equipid, equipMap.size());
            return null;
        }
        int interval_sec = (int) (new Date().getTime() - equipSession.getTime().getTime()) / 1000;
        //2倍的上报周期
        if (interval_sec > 2 * getTermPeriod(equipSession.getTermid())) {
            return null;
        }
        return new EquipNewRsp(equipid, equipSession.getDefname(), equipSession.getType(), equipSession.getStatus(), null);
    }

//    //根据ID更新设备的状态
//    public void updateEquStaByCtrl(int equipid, byte onoff, Date date){
//        EquipSession equipSession = equipMap.get(equipid);
//        equipSession.setStatus(onoff);
//        equipSession.setTime(new Date());
//        equipmentActionDAO.insert(new EquipActionDO(equipid, onoff, (byte) 0, date));
//    }

    //根据termid/addr/reg获取paramid
    public int getParmid(int termid, int addr, int reg) {
        Iterator<SensorParamSession> paramitor = paramMap.values().iterator();
        while (paramitor.hasNext()) {
            SensorParamSession paramSession = paramitor.next();
            if (paramSession.getTermid() == termid
                    && paramSession.getAddr() == addr
                    && paramSession.getReg() == reg) {
                return paramSession.getParamid();
            }
        }
        return 0;
    }

    //根据termid/addr获取生产单元类型
    public String getCellType(int termid, int addr) {
        Iterator<SensorParamSession> paramitor = paramMap.values().iterator();
        while (paramitor.hasNext()) {
            SensorParamSession paramSession = paramitor.next();
            if (paramSession.getTermid() == termid && paramSession.getAddr() == addr)
            {
                int cellid = paramSession.getCellid();
                CellDO cdo = cellDAO.selectById(cellid);
                return cdo.getType();
            }
        }
        return null;
    }

    public boolean isTermExist(int termid) {
        return termMap.containsKey(termid);
    }

    public int getTermIdByEUI(String eui) {
        Iterator<TermSession> termitor = termMap.values().iterator();
        while (termitor.hasNext()) {
            TermSession term = termitor.next();
            if (eui.equals(term.getDeveui()))
                return term.getTermid();
        }
        return 0;
    }

    /**
     * @Description AG21终端注册响应
     **/
    public boolean isAG21PhyInfo(int tid) {
        TermSession tsession = termMap.get(tid);
        if (tsession != null && tsession.getPhysnrlst() != null && tsession.getPhysnrlst().size() > 0)
            return true;
        else
            return false;
    }

    /**
     * @Description AG21终端注册响应
     **/
    public PhySnrInfo getTermPhyInfo(int tid, int addr) {
        TermSession tsession = termMap.get(tid);
        if (tsession != null && tsession.getPhysnrlst() != null) {
            for (PhySnrInfo phy : tsession.getPhysnrlst()) {
                if (phy.getAddr() == addr)
                    return phy;
            }
        }
        return null;
    }

    /**
     * @Description 获取终端状态信息：收到数据包，上报周期，平台成功数据包, 终端电量
     *              获取终端下接入的物理传感器状态信息：传感器上报时间、传感器状态
     * 针对气象站使用
     **/
    public TermStatus getNewTermStat(int termid) {
        TermSession tsession = termMap.get(termid);
        TermStatus termStatus = new TermStatus(tsession);
        termStatus.setPhyStas(getSensorSta(termid));
        return termStatus;
    }

    /**
     * 周期扫描终端、传感器、控制设备Map缓存
     */
    @Scheduled(fixedDelay = SCANSEC * 1000)
    private synchronized void periodTimeScan() {
        int itercnt=0;
        int tocnt=0;
        List<Integer> outTermLst = new ArrayList<>();

        //两小时进行一次统计保存
        if (cycnt++ >= 60 * 60 / SCANSEC * 2) {
            int recNum=0;
//        if (cycnt++ >= 2) {
            Iterator<TermSession> iter = termMap.values().iterator();
            while (iter.hasNext()) {
                TermSession termSession = iter.next();
                if(termSession.getToplat() == 1) {
                    recordChange(termSession);
                    recNum++;
                }
                termSession.setRecvnum(0);
                termSession.setPlatnum(0);
            }
            cycnt=0;
            log.info("-------------------2 hour Scan make {} records in platstat.---------------------", recNum);
        }

        Iterator<TermSession> iter = termMap.values().iterator();
        while (iter.hasNext()) {
            itercnt++;
            TermSession termSession = iter.next();
            if (!termOLChk(termSession)) { //检查是否在线
                outTermLst.add(termSession.getTermid());
                tocnt++;
            }
        }
        for (int tid : outTermLst)
            termMap.remove(tid);
        log.info("{}'st period scan, {} out of {} timeover term has been removed!", cycnt,  tocnt, itercnt);
    }

    /**
     * 两小时进行一次统计保存
     */
    private void recordChange(TermSession ts) {
        int rvnum = ts.getRecvnum();
        int ptnum = ts.getPlatnum();
        int rsec = ts.getRptsec();
        int dc = ts.getDatacyc();
        float succr=0;
        if(rvnum!=0)
            succr = (float) (ptnum * 1.0 / rvnum);
        platDAO.insert(new PlatStatDO(ts.getTermid(), ts.getRptsec(),
                rvnum, ptnum, succr, ts.getVolt(), new Date()));
//        if (succr < 0.8) {
//            //成功率低，延长上报时间60s，设置下发ACK标志
//            ts.setRptchg(true);
//            ts.incrRptSec();
//        } else if (succr > 0.95 && rsec> dc*1.5) {
//            //成功率高，减少上报时间60s，设置下发ACK标志
//            ts.setRptchg(true);
//            ts.decRptSec();
//        }
        if (succr > 0.95 && rsec> dc*1.3) {
            //成功率高，减少上报时间60s，设置下发ACK标志
            ts.setRptchg(true);
            ts.decRptSec();
        }
    }

    private void updateSensor(SensorParamSession paramSession, ReportData report, Date now) {
        //覆盖或新增
        paramSession.setValue(report.getValue());
        paramSession.setActive(true);
        paramSession.setTime(now);
    }

    private void updateEquipReport(EquipSession eqSession, ReportData report, int ctlmode, Date now) {
        int road = eqSession.getRoad();
        int value = (int) report.getValue();
        short mask = (short) (1 << (road - 1) & 0xffff);
        byte cur_onoff = (byte) ((value & mask) >> (road - 1)); //0:关闭  或 1:打开
        if (eqSession.getStatus() != cur_onoff) {
            equipmentActionDAO.insert(new EquipActionDO(eqSession.getId(), cur_onoff, (byte) ctlmode, new Date()));
        }
        eqSession.setStatus(cur_onoff);
        eqSession.setActive(true);
        eqSession.setTime(now);
    }

    /**
     * 延长终端上报时间间隔60s
     */
    public int incrTermPeriod(int termid){
        if(!termMap.containsKey(termid)){
            log.error("incrTermPeriod fail, termid={} not in the termMap, use DataCycle Conf", termid);
            return termService.getTermDataCycle(termid);
        }
        TermSession tsession =termMap.get(termid);
        int before = tsession.getRptsec();
        int after = tsession.incrRptSec();
        log.info("increase termid={} period, before={}, after={}.", termid, before, after);
        return after;
    }

    /**
     * 获取终端动态上报时间间隔
     */
    public int getTermPeriod(int tid){
        int ackrptime = 900;
        if(termMap.containsKey(tid)) {
            ackrptime = termMap.get(tid).getRptsec();
//            log.info("get term={} period={} from termMap", tid, ackrptime);
        }
        else{
            ackrptime = termService.getTermDataCycle(tid);
            log.error("get term period={} from DataCycle Conf", ackrptime);
        }

        return ackrptime;
    }

    /**
     * 获取终端的上报平台类型
     */
    public int getTermPlat(int tid){
        return termService.getTermToPlat(tid);
    }

    /**
     * 更新终端消息数量
     */
    public void updateTermNum(int tid, boolean succfg){
        if(termMap.containsKey(tid)){
            termMap.get(tid).incrCounter(succfg);
        }
    }

    /**
     * 检测终端、传感器参数、设备的在线(超时)处理
     */
    private boolean termOLChk(TermSession ts){
        int per_sec = getTermPeriod(ts.getTermid())*1000;
        long now = System.currentTimeMillis();
        //等待iot终端响应超时，需要删除map中的记录ts
//        log.info("termOLChk now={}, termsession.time={}, per_sec*3={}", now, ts.getTime().getTime(), per_sec*3);
        if (now - ts.getTime().getTime() > per_sec*3) {
            log.error("termid={} not recv report, last report is {}, report_period={}",  ts.getTermid(), sdf.format(ts.getTime()),per_sec);
            termLogOut(ts.getTermid());
            return false;
        }else{
            //单独检查每个环境参数是否超时，设置状态false
            for(int pid:ts.getSensorparams()){
                SensorParamSession sp = paramMap.get(pid);
                if(now - sp.getTime().getTime() > per_sec*3){
                    log.error("snpid={} in termid={} not recv report, last report is {}", pid, ts.getTermid(), sdf.format(sp.getTime()));
                    sp.setActive(false);
                }
            }
            //单独检查每个设备状态是否超时，设置状态false
            for(int eid:ts.getEquiplst()){
                EquipSession es = equipMap.get(eid);
                if(now - es.getTime().getTime() > per_sec*3){
                    log.error("eqpid={} in termid={} not recv report, last report is {}", eid, ts.getTermid(), sdf.format(es.getTime()));
                    es.setActive(false);
                }
            }
            return true;
        }
    }


    /**
     * 获取最后一条数据上报的时间
     * @param termid  终端id
     */
    private String getTermRpTimeDB(int termid){
        List<EnvirDataDO> envlist= envdao.selectList(new EntityWrapper<EnvirDataDO>()
                .eq("termid", termid).orderBy("id", false).last("LIMIT 1"));
        if(envlist!=null&&!envlist.isEmpty()){
            return sdf.format(envlist.get(0).getTime());
        }else{
            return null;
        }
    }

    public IoSession getTermIoSession(int termid){
        return termMap.get(termid).getIosession();
    }

    /**
     * 终端主动下线处理
     */
    public synchronized void termLogOut(int termid){
        TermSession ts = termMap.get(termid);
        if(ts==null){
            log.error("termid={} logout, but not exist in session.", termid);
            return;
        }
        //单独检查每个环境参数是否超时
        for(int pid:ts.getSensorparams()){
            paramMap.remove(pid);
        }
        //单独检查每个设备状态是否超时
        for(int eid:ts.getEquiplst()){
            equipMap.remove(eid);
        }

        IoSession session = ts.getIosession();
        if(session!=null){
            session.closeNow();
        }

        termMap.remove(termid);
    }

    /**
     * 网络原因引起的终端隐式下线
     */
    public void connClose(IoSession iosession){
        for(TermSession item : termMap.values()) {
            if(item.getIosession()!=null && item.getIosession().getRemoteAddress().equals(iosession.getRemoteAddress())){
                log.info("删除Terminal{}的会话Session!", item.getTermid());
//                termMap.remove(item.getTermid());
                termLogOut(item.getTermid());
                return;
            }
        }
    }

    /**
     * 获取终端的状态
     */
    public List<TermSta> getTermSta(String usrname, boolean snep){
        List<TermSta> tslist = new ArrayList<>();
        List<TermDO> terms= null;
        if(usrname!=null) {
            terms = termService.listUsrTerm(usrname);
        }else{
            terms = termService.listAllTerm();
        }
        for(TermDO term:terms){
            TermSta tmsta = new TermSta(term);
            int tid = term.getId();
            //检查是否在线
            if(termMap.containsKey(tid)){
                TermSession  ts =  termMap.get(tid);
                tmsta.setStatus(true);
                tmsta.setIntime(sdf.format(ts.getIntime()));
                if(snep){
                    tmsta.setPhyList(getSensorSta(tid));
                }
            }else{
                tmsta.setStatus(false);
                tmsta.setOuttime(getTermRpTimeDB(tid));
            }
            tslist.add(tmsta);
        }

        return tslist;
    }

    /**
     * 获取终端下面接入传感器的状态
     */
    private List<SnPhySta> getSensorSta(int tid){
        List<SnPhySta> stalist = new ArrayList<>();
        List<SensorPhyDO> phylist = senService.termPhySensors(tid);
        for(SensorPhyDO phy:phylist){
            SnPhySta snsta = new SnPhySta(phy.getId(), phy.getName());
            if(!phy.getProduct().toLowerCase().contains("relay")){
                //处理传感器
                for(SensorParamSession sp:paramMap.values()){
                    if(phy.getId() == sp.getPhyid()){ //终端下传感器保存在map，离线也不会删除
                        snsta.setPhystatus(sp.isActive());
                        snsta.setLstime(sdf.format(sp.getTime()));
                        break;
                    }
                }
            }else{
                //处理继电器
                for(EquipSession ep : equipMap.values()){
                    if(phy.getAddr() == ep.getAddr()){
                        snsta.setPhystatus(ep.isActive());
                        snsta.setLstime(sdf.format(ep.getTime()));
                    }
                }
            }
            stalist.add(snsta);
        }
        return stalist;
    }


    @Data
    public static class TermSta{
        public int termid;
        public String termname;
        public String usrname;
        public boolean status;
        public String intime;
        public String outtime;
        public List<SnPhySta> phyList;

        public TermSta(TermDO tdo){
            termid = tdo.getId();
            termname = tdo.getName();
            usrname = tdo.getUsername();
            status = false;
        }
    }

    @Data
    public static class SnPhySta {
        public int phyid;
        public String phyname;
        public boolean phystatus;
        public String lstime;

        public SnPhySta(int snid, String name){
            phyid = snid;
            phyname = name;
            phystatus=false;
        }
    }
}
