package edu.jit.nsi.iot_ms.serviceimpl.impl;

import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.ReportData;
import edu.jit.nsi.iot_ms.transport.httpclient.HttpClientGate;
import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJCWHData;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EPServiceImpl {
    private static int ELESIZE = 11;
    @Autowired
    SensorCmdCfg sensorCmdCfg;

    @Autowired
    SessionManager sessMngr;

    @Autowired
    TerminalServiceImpl terminalService;

    @Autowired
    IotServiceImpl iotService;

    @Autowired
    private HttpClientGate httpCli;

    public void msghandler(IoSession session, Object message) {
        String str = message.toString();
        String clientAddress = session.getRemoteAddress().toString().replace("/", "");
        log.info("接受来自 {} 的信息长度为 {}", clientAddress, str.length());

        List<ReportData> reports=new ArrayList<ReportData>();
        JSPlatJCWHData jsrpt = new JSPlatJCWHData();

        String devEUI = buildReportData(str, reports, jsrpt);
        if(devEUI == null){
            log.error("农芯EP400终端上报数据格式异常!");
            return;
        }

        int termid = sessMngr.getTermIdByEUI(devEUI);
//        log.info("deveui {}, as termid {}, report {} datas.", devEUI, termid, reports.size());
        //收到终端第一条消息则创建缓存
        if(termid == 0){
            termid = terminalService.getTermIdEui(devEUI);
            if(termid == 0){
                log.error("get lora terminal deveui{} not exist.", devEUI);
                return;
            }
            int dc = terminalService.getTermDataCycle(termid);
            int plat = terminalService.getTermToPlat(termid);
            sessMngr.putOneSess(3, termid, dc,plat,null, devEUI);
//            log.info("init deveui {}, termid {} session ", devEUI, termid);
        }

        //插入数据库
        iotService.recordReport(termid, reports, 1);
        int toplat = sessMngr.getTermPlat(termid);
        if(toplat==1){
            boolean msgRptRes = httpCli.postMsg2JSPlat(jsrpt);
            if(msgRptRes){
                log.info("EP deveui:{} report xxx msg to JSIOTPlat succeed.", devEUI);
            }else{
                log.error("EP deveui:{} report xxx msg to JSIOTPlat fail.", devEUI);
            }
        }
//        sessMngr.updateSessByReport(termid, reports,0);
        log.info("deveui:{}, termid:{} 周期上报消息处理成功!", devEUI, termid);
    }

    /**
     * 连接关闭
     * @param session 连接信息
     */
    public void connClose(IoSession session){
        sessMngr.connClose(session);
    }


    /**
     * 创建上报对象
     * @param str 上报的消息
     */
    private String buildReportData(String str, List<ReportData> reports, JSPlatJCWHData jsrpt){
        String[] elements = str.split(",");
        if(elements.length != ELESIZE || !elements[0].equals("401")){
            return null;
        }

        //只获取传感器+电量
        for (int i = 3; i < 10; i++) {
            //注意这里的type是大写的
            reports.add(new ReportData(1, i, sensorCmdCfg.getEP400FrameType(i), Float.parseFloat(elements[i])));
        }
        //获得串号
        return elements[1];
    }
}
