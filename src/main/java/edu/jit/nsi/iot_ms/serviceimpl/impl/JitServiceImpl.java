package edu.jit.nsi.iot_ms.serviceimpl.impl;

import edu.jit.nsi.iot_ms.commons.util.JacksonUtils;
import edu.jit.nsi.iot_ms.config.RelayCtlCmd;
import edu.jit.nsi.iot_ms.config.SensorCmd;
import edu.jit.nsi.iot_ms.serviceimpl.TermRspHandler;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.ReportData;
import edu.jit.nsi.iot_ms.transport.httpclient.HttpClientGate;
import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJCWHData;
import edu.jit.nsi.iot_ms.transport.tcp.payload.tcpLoginRsp;
import edu.jit.nsi.iot_ms.transport.tcp.payload.tcpRecvData;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Slf4j
@Service
public class JitServiceImpl {
    private static final String CMD_REGIST = "register";
    private static final String CMD_SENSORCFG = "sensorcfg";
    private static final String CMD_BYE = "bye";
    private static final String CMD_REPORT = "report";
    private static final String CMD_COMM = "comm";
    private static final String RSP_QUERY = "query";
    private static final String RSP_CONTROL = "control";
    private static final int SCAN_PER = 8000;
    //private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Map<Integer, TermRspHandler> toRspAppMap = new HashMap<>();  //等待APP响应
    private Map<Integer,Long> iotStartMap = new HashMap<>();            //起始操作终端时间
    private int ctlmode = 0;   //0:by app , 1:by hand, 2:by computer

    @Autowired
    IotServiceImpl iotservice;
    @Autowired
    EquipServiceImpl equipService;
    @Autowired
    TerminalServiceImpl terminalService;
    @Autowired
    SessionManager sessMngr;
    @Autowired
    private HttpClientGate httpCli;

    //初始化循环检测线程
//    @PostConstruct
//    public void init(){
//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                periodScanMap();
//            }
//        }).run();
//    }

    public void msghandler(IoSession session, Object message) {
        String str = message.toString();
        String clientAddress = session.getRemoteAddress().toString().replace("/", "");

        tcpRecvData recieveData = JacksonUtils.readValue(str, tcpRecvData.class);
        if(recieveData== null) {
            log.error("地址为 {} 的 {} 消息解码错误", clientAddress, str);
            session.write("{\"terminal\":\"server\",\"msgType\":\"msgERROR\"}\r\n");
            return;
        }
        log.info("接受来自 {} 的终端id:{}, 信息类型为:{}, 长度为:{}", clientAddress, recieveData.getTermid(), recieveData.getMsgType(), str.length());

        //添加终端termid的检验，检查termid是否已经注册
        //TODO

        String cmd = recieveData.getMsgType();
        switch (cmd) {
            case CMD_REGIST:
            case CMD_SENSORCFG:
                onsnsrcfg(session, recieveData);
                break;
            case CMD_BYE:
                offLine(recieveData);
                break;
            case CMD_REPORT:
                reportData(session, recieveData);
                break;
            case CMD_COMM:
                onComm(session, recieveData);
                break;
            case RSP_QUERY:
                onQuery(session, recieveData);
                break;
            case RSP_CONTROL:
                onControl(session, recieveData);
                break;
            default:
                log.error("Unkown Msg Type, Ignore this msg!!!");
                break;
        }
        return ;
    }

    /**
     * 注册上线
     * @param session   会话信息
     * @param recieveData 消息对象
     */
    void onsnsrcfg(IoSession session, tcpRecvData recieveData) {
        if("device".equalsIgnoreCase(recieveData.getTerminal())) {
            int tid = recieveData.getTermid();
            int dc = terminalService.getTermDataCycle(tid);
            int plat = terminalService.getTermToPlat(tid);
            sessMngr.putOneSess(1,tid, dc, plat, session,null);
            log.info("ID为 {} 的网关上线了!", tid);

            //返回传感器485指令集
            List<SensorCmd> sensors = new ArrayList<>();
            List<RelayCtlCmd> relays = new ArrayList<>();
            iotservice.termSRList(tid, sensors, relays);
            tcpLoginRsp reply = new tcpLoginRsp("server", recieveData.getTermid(),
                    "register", sensors, relays);
            session.write(JacksonUtils.toJson(reply) + "\r\n");
//            log.info("发送注册响应:{}", JacksonUtils.toJson(reply));
        }
    }

    /**
     * 周期上报
     *
     * @param session     会话
     * @param recieveData 消息对象
     * @throws ParseException
     */
    void reportData(IoSession session, tcpRecvData recieveData){
        int tid = recieveData.getTermid();
        List<ReportData> reports = recieveData.getContent();
        JSPlatJCWHData jsrpt = new JSPlatJCWHData();
        //更新缓存数据
//        sessMngr.updateSessByReport(tid, reports);
        //周期上报数据入库
        iotservice.recordReport(tid, reports, 1);

        int toplat = sessMngr.getTermPlat(tid);
        if(toplat==1){
            boolean msgRptRes = httpCli.postMsg2JSPlat(jsrpt);
            if(msgRptRes){
                log.info("JIT embed termid:{} report xxx msg to JSIOTPlat succeed.", tid);
            }else{
                log.error("JIT embed termid:{} report xxx msg to JSIOTPlat fail.", tid);
            }
        }
        log.info("终端ID:{} 周期上报消息处理成功!", tid);
    }


    /**
     * server端的查询/控制透传功能，将query与control消息的请求与响应在APP与终端之间透传
     *  comm消息应该收不到了
     * @param session     会话
     * @param recieveData 消息对象
     */
    public void onComm(IoSession session, tcpRecvData recieveData) {
        recieveData.setTo(session.getRemoteAddress().toString().replace("/", ""));//设置To
        boolean dtfg = dt2Term(recieveData);
        if (!dtfg) {
            recieveData.setTerminal("server");
            session.write(JacksonUtils.toJson(recieveData) + "\r\n");
        }
    }

    public boolean sendCntl(int clm, int termid, int addr, int road, int onoff, TermRspHandler rsphandler){
        ctlmode = clm;

        //检查是否有对该
        if(toRspAppMap.containsKey(termid)){
            return false;
        }
        toRspAppMap.put(termid, rsphandler);
        iotStartMap.put(termid, System.currentTimeMillis());
        //构造recvdata
        tcpRecvData ctnlReq = new tcpRecvData();
        ctnlReq.setTerminal("deivice");
        ctnlReq.setTermid(termid);
        ctnlReq.setMsgType("control");
        ctnlReq.setOrder(addr+"#"+road+"#"+onoff);
        ctnlReq.setTo(Integer.toString(1));
        return dt2Term(ctnlReq);
    }

    void onQuery(IoSession session, tcpRecvData recieveData){
        //处理
        int termid = recieveData.getTermid();
        TermRspHandler rsphandler= toRspAppMap.get(termid);
        if(rsphandler!=null){
            rsphandler.execute();
        }
        toRspAppMap.remove(termid);
        iotStartMap.remove(termid);
        log.info("处理ID为{}的终端实时查询消息", termid);
    }

    void onControl(IoSession session, tcpRecvData recieveData){
        int termid = recieveData.getTermid();
        //更新继电器操作和设备状态记录
        //更新缓存数据
        sessMngr.updateSessByReport(termid, recieveData.getContent(),ctlmode);
//        iotservice.recordReport(termid, recieveData.getContent(),ctlmode);
        TermRspHandler rsphandler= toRspAppMap.get(termid);
        //必须先remove在执行回调
        toRspAppMap.remove(termid);
        iotStartMap.remove(termid);
        log.info("处理ID为{}的终端实时控制消息成功,执行回调", termid);

        if(rsphandler!=null){
            rsphandler.execute();
        }
    }

    /**
     * 等待响应超时
     * @param termid 终端id
     */
    void onTimeOut(int termid){
        TermRspHandler rsphandler= toRspAppMap.get(termid);
        //必须先remove在执行回调
        toRspAppMap.remove(termid);
        iotStartMap.remove(termid);
        log.info("处理ID为{}的终端等待终端响应超时, 剩余map size={}，执行回调", termid, toRspAppMap.size());

        if(rsphandler!=null){
            rsphandler.timeout();
        }
    }

    /**
     * 主动下线
     * @param recieveData 消息对象
     */
    void offLine(tcpRecvData recieveData) {
        int tid = recieveData.getTermid();
        //将网关的数据在map中删除
        sessMngr.termLogOut(tid);
    }

    /**
     * 连接关闭，隐式下线
     * @param session 连接信息
     */
    public void connClose(IoSession session){
        sessMngr.connClose(session);
    }

    /**
     * 周期扫描Map中的等待响应缓存
     */
    @Scheduled(fixedDelay = SCAN_PER)
    private void periodScanMap() {
        Set<Integer> termids = iotStartMap.keySet();
//      log.info("目前控制请求的数量:{}", termids.size());
        for (int tid : termids) {
            long start = iotStartMap.get(tid);
            long now = System.currentTimeMillis();
            //等待iot终端响应超时，需要删除map中的记录
            if (now - start > SCAN_PER) {
                onTimeOut(tid);
            }
        }
    }


    /**
     * 将APP的请求透传给终端
     */
    private boolean dt2Term(tcpRecvData recieveData){
        int tid = recieveData.getTermid();
        //APP请求，透传给网关
        boolean exists=sessMngr.isTermExist(tid);
        if (!exists) {
            //网关不在线
            log.info("请求网关不在线:{}", tid);
            return false;
        }
        //透传给网关
        IoSession iosession = sessMngr.getTermIoSession(tid);
        iosession.write(JacksonUtils.toJson(recieveData) + "\r\n");
        log.info("向GW{}透传消息:{}",iosession.getRemoteAddress().toString(),JacksonUtils.toJson(recieveData));

        return true;
    }

}
