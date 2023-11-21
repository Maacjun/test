package edu.jit.nsi.iot_ms.serviceimpl.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;

import edu.jit.nsi.iot_ms.Fegin.Authority.AuFeignClient;
import edu.jit.nsi.iot_ms.Fegin.SMS.SMSFeignClient;
import edu.jit.nsi.iot_ms.domain.*;
import edu.jit.nsi.iot_ms.mapper.*;

import edu.jit.nsi.iot_ms.transport.msg.EnviHisAggrRsp;
import edu.jit.nsi.iot_ms.serviceimpl.TermRspHandler;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.transport.tcp.EquipNewRsp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

enum ParamLvl {
    // 因为已经定义了带参数的构造器，所以在列出枚举值时必须传入对应的参数
    DWACT(0), DWWARN(1), NORMAL(2), UPWARN(3), UPACT(4);

    // 定义一个 private 修饰的实例变量
    private int level;

    // 定义一个带参数的构造器，枚举类的构造器只能使用 private 修饰
    private ParamLvl(int d) {
        this.level = d;
    }
    // 定义 get set 方法
    public  int getDate() {
        return level;
    }
    public void setDate(int d) {
        this.level = d;
    }
}

@Slf4j
@Service
public class EvnCtlServiceImpl {
    public static final int Scan_Min = 30;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat tm_sdf = new SimpleDateFormat("HH:mm:ss");
    private Map<String, Integer> cltCellMap = new HashMap<>();
    private String tm_last = null;
    private String dt_last = null;

    @Autowired
    AppServiceImpl appService;

    @Autowired
    EnvThCtlDAO envThCtlDAO;

    @Autowired
    EnvCmpCtlDAO envCmpCtlDAO;

    @Autowired
    EnvTmCtlDAO envTmCtlDAO;

    @Autowired
    EquipServiceImpl equipService;

    @Autowired
    WarnDAO warnDAO;

    @Autowired
    AutoActionDAO atactDAO;

    @Autowired
    JitServiceImpl minaService;

    @Autowired
    TerminalServiceImpl termService;

    @Autowired
    LoraServiceImpl loraService;

    @Autowired
    CellDAO cellDAO;



//    @Qualifier("edu.jit.nsi.iot_ms.Fegin.SMS.SMSFeignClient")
    @Autowired
    SMSFeignClient smsFeignClient;
    @Autowired
    AuFeignClient auFeignClient;
    @Autowired
    sendmessageDAO send;



    /**
     * 告警日志记录
     * @param cell_id  生产单元集合
     * @param start_time  开始时间
     * @param end_time  结束时间
     */
    public List<WarnDO> warnQuery(int cell_id, String start_time, String end_time){
        return warnDAO.selectList(new EntityWrapper<WarnDO>()
                .eq("cellid", cell_id).between("time", start_time, end_time));
    }


    /**
     * 自动控制日志记录
     * @param cell_id  生产单元集合
     * @param start_time  开始时间
     * @param end_time  结束时间
     */
    public List<AutoActionDO> autoActQuery(int cell_id, String start_time, String end_time){
        return atactDAO.selectList(new EntityWrapper<AutoActionDO>()
                .eq("cellid", cell_id).between("time", start_time, end_time));
    }


    /**
     * 延迟（5秒钟）依次打开或关闭塘口下的所有设备
     * @param type 环境参数
     * @param optfg 操作指令 0:关闭   1:打开
     * @return
     */
    private synchronized boolean delayOptEqp(int cellId, String type, int optfg, float value, Date date){
        //首先判断是否已经操作完毕
        if(cltCellMap.containsKey(cellId+"#"+type)&&cltCellMap.get(cellId+"#"+type)==optfg) {
            if(optfg==1)
                log.info("自动控制[{}]塘口[{}]环境参数已启动.", cellId, type);
            else
                log.info("自动控制[{}]塘口[{}]环境参数已关闭.", cellId, type);
            return true;
        }
        log.info("自动控制[{}]塘口[{}]环境参数[{}]启动...", cellId, type, optfg);
        //打开塘口下所有类型为type的设备

        List<EquipDO> eqplist = equipService.cellParamEqp_list(cellId,type);//获取生产单元下该参数类型的所有设备信息
        List<EquipNewRsp> equipStaList = appService.onCellEqpQuery(cellId);//返回所有设备最新的状态
        for(EquipDO eq : eqplist){//遍历该生产单元下控制该类型的设备
            for(EquipNewRsp eqsta : equipStaList){//遍历生产单元下所有设备
                if(eqsta.getEpid() == eq.getId()&&eqsta.getStatus()!=optfg) {//首先判断是否为同一设备，
                    // 在判断状态和操作指令是否冲突（这一步还挺重要的，ctrlCellEquip中的回调函数再次调用delayOptEqp（）那么eqsta.getStatus()==optfg也就会输出操作已完成）
                    ctrlCellEquip(eq, optfg, value, date);//操作设备
                    return false;
                }
            }
        }

        //所有设备都控制成功后，记录状态
        log.info("自动控制[{}]塘口[{}]环境参数[{}]操作已完成.", cellId, type, optfg);
        cltCellMap.put(cellId + "#" + type, optfg);
        atactDAO.insert(new AutoActionDO(cellId, type, optfg, value, date));
        return true;
    }



    private void ctrlCellEquip(EquipDO equipData, int optfg, float value, Date date){
        TermDO termDO= termService.getTermById(equipData.getTermid());

        TermRspHandler handler = new TermRspHandler() {
            //控制成功要执行回调函数
            public void execute() {
                log.info("control succeed. call callback");
                delayOptEqp(equipData.getCellid(), equipData.getType(), optfg, value, date);
            }

            //控制超时要执行回调函数
            public void timeout() {
                log.info("control timeover. call callback");
                delayOptEqp(equipData.getCellid(), equipData.getType(), optfg, value, date);
            }
        };
        if(termDO.getType()==1) {
            //通过minaservice发送control请求，并注册回调函数
            minaService.sendCntl(2, equipData.getTermid(),
                    equipData.getAddr(), equipData.getRoad(), optfg, handler);
        }else if(termDO.getType()==5){
            loraService.downLinkCtrl(2, termDO.getDeveui(),
                    equipData.getAddr(), equipData.getRoad(), optfg, handler);
        }
    }


    private ParamLvl paramLevel(float value , EnvThrsdCtlDO envctl){
        if(value < envctl.getActdw())
            return ParamLvl.DWACT;
        else if(value < envctl.getWndw())
            return ParamLvl.DWWARN;
        else if(value < envctl.getWnup())
            return ParamLvl.NORMAL;
        else if(value < envctl.getActup())
            return ParamLvl.UPWARN;
        else
            return ParamLvl.UPACT;
    }

    /**
     * 延迟（5秒钟）依次打开或关闭塘口下的所有设备
     * @param values 塘口ID
     * @param envctl 环境参数
     * @return  DWACT:下限操作值, DWWARN:下限告警值, NORMAL:正常值, UPWARN:上限告警值, UPACT:上限操作值
     */
    private ParamLvl judgePL(List<Float> values , EnvThrsdCtlDO envctl){
        int[] nums = {0,0,0,0,0};//用来存储参数中各个阈值出现的次数
        int maxpos=0;
        for(Float va : values){
            ParamLvl lvl = paramLevel(va , envctl);//判断参数的值处于哪个阶段（操作下限，告警下限，正常值，告警上限，操作上限）
            nums[lvl.ordinal()]++;//lvl.ordinal()是枚举的序列号
        }

        for(int i=0; i<5; i++){
            if(nums[i]>nums[maxpos])
                maxpos = i;
        }

        return ParamLvl.values()[maxpos];//返回出现次数修多的阈值
    }

    /**
     * 基于当前设备状态与配置的差值,判断是否启动设备
     * @param sntv1 传感器参数值1
     * @param sntv2 传感器参数值2
     * @return  1:设备动作   0:保持
     */
    private boolean judgeCmp(EnvCmpCtlDO autoCell, EnviHisAggrRsp.SNTV sntv1, EnviHisAggrRsp.SNTV sntv2){
        List<Float> values1 =sntv1.getValues();
        List<Float> values2 =sntv2.getValues();

        //获取设备状态
        int pos=0;
        int cnt=0;
        while(pos<values1.size()&&pos<values2.size()){
            if(autoCell.isGrthn()) {
                if (values1.get(pos) - values2.get(pos) > autoCell.getDist())
                    cnt++;
            }else{
                if(values1.get(pos)-values2.get(pos)<autoCell.getDist())
                    cnt++;
            }
            pos++;
        }
        if(cnt>pos*0.8)
            return true;
        else
            return false;
    }
    /**
     * 基于参数值对比操作
     * @param now 当前时间
     * @param start 前一轮(开始)时间
     * @param end  当前(结束)时间
     * @return  null
     */
    private void cmpHandle(Date now, String start, String end) {

        List<EnvCmpCtlDO> autoctl = envCmpCtlDAO.selectList(new EntityWrapper<EnvCmpCtlDO>());
        for (EnvCmpCtlDO autoCell : autoctl) {
            int cellId = autoCell.getCellid();
            boolean isAuto = autoCell.getAutofg()==1? true: false;
            if(!isAuto)
                continue;
//            log.info("[cmpHandle] cellid{} start...", cellId);
            EnviHisAggrRsp.SNTV sntv1 = appService.onParamHisQuery(autoCell.getParamid1(), start, end);
            EnviHisAggrRsp.SNTV sntv2 = appService.onParamHisQuery(autoCell.getParamid2(), start, end);
            if(sntv1.getValues().size()==0||sntv2.getValues().size()==0)
                continue;
            String pa = autoCell.getParam();
            float lstva = sntv1.getValues().get(sntv1.getValues().size() - 1);
            boolean isbeyond = judgeCmp(autoCell, sntv1, sntv2);
//            log.info("[cmpHandle] cellid{} judge result, isbeyond is{}.", cellId, isbeyond);
            //若超过定义的distance,则根据设备状态来触发动作
            if(isbeyond){
                delayOptEqp(cellId, pa, autoCell.getOpt(), lstva, now);
                //下面打印可参考没有实际操作
                List<EquipNewRsp> equpStaLst = appService.paramEqpQuery(cellId, pa);
                for(EquipNewRsp equipsta: equpStaLst){
                    log.info("[cmpHandle] the differentce between {} and {} beyond distance:{}, " +
                            "equipid {}'s current stat:{}, def opt:{}", autoCell.getParamid1(), autoCell.getParamid2(),
                            autoCell.getDist(), equipsta.getEpid(), equipsta.getStatus(), autoCell.getOpt());

                    if(equipsta.getStatus()!=autoCell.getOpt()){
                        log.info("[cmpHandle] execute opt{}!!!", autoCell.getOpt());

                    }
                }
            }

        }

    }


    /**
     * 基于阈值的检查操作
     * @param now 当前时间
     * @param start 前一轮(开始)时间
     * @param end  当前(结束)时间
     * @return  null
     */
    private void thresdHandle(Date now, String start, String end) throws SQLException {
        log.info("基于阈值的检查操作 thresdHandle() start  run");
        List<EnvThrsdCtlDO> autoctl = envThCtlDAO.selectList(new EntityWrapper<EnvThrsdCtlDO>());//设备阈值（告警，自动控制）表
        for (EnvThrsdCtlDO autoCell : autoctl) {//
            int cellId = autoCell.getCellid();//获取生产单元id
            boolean isAuto = autoCell.getAutofg()==1? true: false;//查看是否开启了自动控制
            String pa = autoCell.getParam();//要控制的环境参数名
            EnviHisAggrRsp.SNTV sntv = appService.onParamHisQuery(autoCell.getParamid(), start, end);//某个环境参数的历史数据
            List<Float> values = sntv.getValues();//历史数据的值
            if(values.size() ==0)
                continue;
            float lstva = values.get(values.size() - 1);//最新的环境参数值
            ParamLvl level = judgePL(values, autoCell);//返回环境参数所处阈值级别
            switch (level) {
                case DWACT://操作下限
                    log.info("{}号塘口，{}参数阈值处于 DWACT",cellId,pa);
                    WarnDO autoact_dw = new WarnDO(cellId, pa, true, 1, lstva, now);
                    sendWarnMessage( autoCell,level,lstva);
                    warnDAO.insert(autoact_dw);//在数据库插入异常记录
                    if(isAuto){//是否要自动控制
                        delayOptEqp(cellId, autoCell.getParam(), 1, lstva, now);
                    }
                    break;
                case DWWARN:
                    log.info("阈值处于 DWWARN");
                    WarnDO autowarn_dw = new WarnDO(cellId, pa, true, 0, lstva, now);
                    sendWarnMessage( autoCell,level,lstva);
                    warnDAO.insert(autowarn_dw);
                    break;
                case UPACT:
                    log.info("阈值处于 UPACT");
                    WarnDO autoact_up = new WarnDO(cellId, pa, false, 1, lstva, now);
                    sendWarnMessage( autoCell,level,lstva);
                    warnDAO.insert(autoact_up);
                    if(isAuto){
                        delayOptEqp(cellId, autoCell.getParam(), 1, lstva, now);
                    }
                    break;
                case UPWARN:
                    log.info("阈值处于 UPWARN");
                    WarnDO autowarn_up = new WarnDO(cellId, pa, false, 0, lstva, now);
                    sendWarnMessage( autoCell,level,lstva);
                    warnDAO.insert(autowarn_up);
                    break;

                case NORMAL:
                    log.info("阈值处于 UPWARN");
                    delayOptEqp(cellId, autoCell.getParam(), 0, lstva, now);
                    break;
            }


        }
    }


    /**
     * 基于时间的检查操作
     * @param now 开始时间
     * @param last 前一轮的时间
     * @return  null
     */
    private void timeHandle(String last, String now, Date date_now) {
        List<EnvTimeCtlDO> autoctl = envTmCtlDAO.selectList(new EntityWrapper<EnvTimeCtlDO>().eq("autofg",1));//获取设备时间阈值
        for (EnvTimeCtlDO timeCtl : autoctl) {
            int cellId = timeCtl.getCellid();
            String tmpoint = timeCtl.getTime();
            if(timeBetween(tmpoint, last, now)) {
                delayOptEqp(cellId, timeCtl.getParam(), timeCtl.getOpt(), 0, date_now);
            }
        }
    }


    /**
     *
     * @param nowDate   要比较的时间
     * @param startDate   开始时间
     * @param endDate   结束时间
     * @return   true在时间段内，false不在时间段内
     * @throws Exception
     */
    public static boolean timeBetween(String nowDate, String startDate, String endDate){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date now,start,end;
        try{
            now = format.parse(nowDate);
            start = format.parse(startDate);
            end = format.parse(endDate);
        }catch (Exception e){
            log.error("[timeBetween]format time(String) to 24(HH:mm:ss) error!");
            return  false;
        }

        long nowTime = now.getTime();
        long startTime = start.getTime();
        long endTime = end.getTime();

        return nowTime >= startTime && nowTime <= endTime;
    }


    /**
     * 发信息警告
     * */
    private void sendWarnMessage(EnvThrsdCtlDO key, ParamLvl level, Float lastValue) throws SQLException {

        int temp=0;
        if(level==ParamLvl.DWACT){
            temp=1;
        } else if (level==ParamLvl.DWWARN) {
            temp=2;
        }
        try {
            log.info("正在调用auth-service和SMS服务进行发送警告信息");
            authResponse loginfo = auFeignClient.logIn("SMS", "SMS");
            Object data = loginfo.getData();
            LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) data;
            // 获取 "token" 字段的值
            Object tokenObj = dataMap.get("token");
            String token = (String) tokenObj;
            //使用 token 字符串
//            log.info("Token: {}", token);
            authResponse userinfo=(auFeignClient.userInfoByName("xu",token));
            Object detail = userinfo.getData();
            log.info("查找的用户信息{}", userinfo);
            log.info("detail{}",detail);
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) detail;
            Object tel_ = map.get("tel");
            String userTel = (String) tel_;
            log.info("用户信息，手机号码{}",userTel);
            smsFeignClient.warnMessage(temp, cellDAO.selectById(key.getCellid()).getName() , key.getParam(), lastValue, "0.5", userTel);
            send.insert(new sendmessage(key.getCellid(),key.getParam(),lastValue,new Timestamp(System.currentTimeMillis()),userTel));
        } catch (Exception e) {
            log.error("调用AUTHService服务查询用户失败");

        }
    }


    /**
     * 周期扫描Map中的等待响应缓存
     */
    @Scheduled(fixedDelay = Scan_Min*60*1000, initialDelay=30*1000)
    private void periodChkCtl() throws SQLException {
        log.info("periodChkCtl() start run");
        Date now = new Date();
        if(dt_last==null || tm_last==null){
            Date last = new Date(now.getTime() - Scan_Min * 60 * 1000); //上个扫描周期的时间
            try {
                tm_last = tm_sdf.format(last);
                dt_last = sdf.format(last);
            }catch (Exception e){
                log.error("[periodChkCtl] format date(yyyy) to 24 time(HH:mm:mm) error!");
                return;
            }
        }

        String tm_now=null;
        String dt_now=null;
        try {
            tm_now = tm_sdf.format(now);
            dt_now = sdf.format(now);
        }catch (Exception e){
            log.error("[periodChkCtl] format date(yyyy) to 24 time(HH:mm:mm) error!");
        }

        thresdHandle(now, dt_last, dt_now);
        timeHandle(tm_last, tm_now, now);
        cmpHandle(now,  dt_last, dt_now);
        tm_last = tm_now;
        dt_last = dt_now;
    }
}
