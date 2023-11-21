package edu.jit.nsi.iot_ms.serviceimpl.impl;

import edu.jit.nsi.iot_ms.commons.util.Base64Utils;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.ReportData;
import edu.jit.nsi.iot_ms.transport.httpclient.HttpClientGate;
import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJCWHData;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class EComGWServiceImpl {
    private static int ELESIZE = 63;  //总消息长度
    private static int GW4GSIZE = 64;  //总消息长度
    private static int ACKSIZE = 12;  //总消息长度
    private static int DEVOFFSET = 6;  //deveui偏移
    private static int DATAOFFSET = 35;  //传感器data偏移


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

    public static String HEX = "0123456789ABCDEF";
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    Map<Integer, Integer> termUpSeQ = new HashMap<Integer, Integer>();
    Map<Integer, Integer> termDnSeQ = new HashMap<Integer, Integer>();

    public void msghandler(IoSession session, Object message) {
        IoBuffer buffer = (IoBuffer) message;
        byte[] msgbyte = new byte[buffer.limit()];
        buffer.get(msgbyte,buffer.position(),buffer.limit());

        StringBuffer msgsb = new StringBuffer(msgbyte.length * 2);
        bytesToHexBuff(msgbyte, 0, msgbyte.length, msgsb);
        String msg = msgsb.toString();
        String clientAddress = session.getRemoteAddress().toString().replace("/", "");
        log.info("EComGW 接受来自 {} 的信息长度为 {}, 消息内容:{}", clientAddress, msg.length(), msg);

        List<ReportData> reports=new ArrayList<ReportData>();
        JSPlatJCWHData jsrpt = new JSPlatJCWHData();

        StringBuffer devsb = new StringBuffer(msgbyte.length * 2);
        int upSeq = buildReportData(msgbyte, reports, jsrpt, devsb);
        String devEUI = devsb.toString();
        if(devEUI == null){
            log.error("易通汇联气象站网关上报数据格式异常!");
            return;
        }

        int termid = sessMngr.getTermIdByEUI(devEUI);
        //收到终端第一条消息则创建缓存
        if(termid == 0){
            termid = terminalService.getTermIdEui(devEUI);
            if(termid == 0){
                log.error("get lora terminal deveui{} not exist.", devEUI);
                return;
            }
            int dc = terminalService.getTermDataCycle(termid);
            int plat = terminalService.getTermToPlat(termid);
            sessMngr.putOneSess(3, termid, dc, plat,null, devEUI);
        }


        //判断当前终端的消息序列号是否改变
        if(!termUpSeQ.containsKey(termid)||!termUpSeQ.get(termid).equals(upSeq)) {
            //插入数据库
            iotService.recordReport(termid, reports, 1);
            termUpSeQ.put(termid, upSeq);
            log.info("deveui:{}, termid:{} 周期上报消息处理成功!", devEUI, termid);
        }else{
            log.info("deveui:{}, termid:{} 重传周期上报消息!", devEUI, termid);
        }

        int toplat = sessMngr.getTermPlat(termid);
        if(jsrpt!=null && toplat==1){
            boolean msgRptRes = httpCli.postMsg2JSPlat(jsrpt);
            int rptSec = sessMngr.getTermPeriod(termid);
            sessMngr.updateTermNum(termid,msgRptRes);
            if(msgRptRes){
                //返回成功响应ACK给网关
                byte[] ack = buildAckMsg(termid, rptSec, devEUI);
                session.write(IoBuffer.wrap(ack));
                log.info("Ecomm gw deveui:{}, period:{} report weather msg to JSIOTPlat succeed.", devEUI, rptSec);
            }else{
                //返回成功响应ACK给网关，并增加时延
                rptSec = sessMngr.incrTermPeriod(termid);
                byte[] ack = buildAckMsg(termid, rptSec, devEUI);
                session.write(IoBuffer.wrap(ack));
                log.error("Ecomm gw deveui:{} report weather msg to JSIOTPlat fail, reset peroid:{}", devEUI, rptSec);
            }

            //上报江宁区智慧农业平台
            jsrpt.setSessionKey("121345");
            httpCli.postMsg2JNPlat(jsrpt);
        }

    }

    /**
     * 确认上行包ACK消息，含上报周期的配置下发
     *
     */
    public byte[] buildAckMsg(int termid, int rperiod, String deveui){
        byte bchk = 0;
        if(!termDnSeQ.containsKey(termid)){
            termDnSeQ.put(termid, 0);
        }
        int dwnseq = termDnSeQ.get(termid);
        int upseq = termUpSeQ.get(termid);
//        int rptSec = sessMngr.getTermPeriod(termid);
        dwnseq++;
        byte[] dn_ba = Base64Utils.intToBytes(dwnseq);
        byte[] up_ba = Base64Utils.intToBytes(upseq);
        byte[] rpt_ba = Base64Utils.intToBytes(rperiod);
        termDnSeQ.put(termid, dwnseq);

        byte[] ackmsg = new byte[ACKSIZE];
        ackmsg[0] = (byte) 0xFE;
        ackmsg[1] = dn_ba[0];
        ackmsg[2] = dn_ba[1];
        ackmsg[3] = (byte)0xc0;
        ackmsg[4] = (byte)0x04;
        ackmsg[5] = (byte)0x00;

        ackmsg[6] = up_ba[0];
        ackmsg[7] = up_ba[1];
        ackmsg[8] = rpt_ba[0];
        ackmsg[9] = rpt_ba[1];

        for(int i=1; i<=9; i++)
            bchk += ackmsg[i];
        ackmsg[10] = bchk;
        ackmsg[11] = (byte)0xEF;
//        log.info("Ecomm gw deveui:{} make ACK msg, upCSQ:{}, downCSQ:{}, rptSec:{}.", deveui, upseq, dwnseq, rperiod);
        return ackmsg;
    }

    public static void bytesToHexBuff(byte[] bytes, int start, int len, StringBuffer sb) {
        for (int i=start;i<start+len;i++) {
            sb.append(HEX.charAt((bytes[i] >> 4) & 0x0f));
            sb.append(HEX.charAt(bytes[i] & 0x0f));
        }
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
     * @param msgba 上报的消息字节数组
     * 返回上报序列号
     */
    private int buildReportData(byte[] msgba, List<ReportData> reports, JSPlatJCWHData jsrpt, StringBuffer devbuf){
        int csq = 0xff;
        int humi = 0xffff;
        int temp = 0xffff;
        int illu = 0xffff;
        int airpress = 0xffff;
        int rain = 0xffff;
        int windspd = 0xffff;
        int winddrt = 0xffff;
        int soilhumi = 0xffff;
        int soiltemp = 0xffff;
        int soiltds = 0xffff;
        int soilec = 0xffff;
        int batvolt = 0xff;
        byte sdata[] = new byte[4];
        int upSeq=-1;
        String deveui;
        String ccid;
//        if(msgba.length != ELESIZE || msgba[0]!=(byte)0xfe && msgba[ELESIZE-1]!=(byte)0xef){
//            log.error("report msg len|start|end illegal, ignore...");
//            return null;
//        }

        if(msgba.length != ELESIZE && msgba.length != GW4GSIZE){
            log.error("report msg len|start|end illegal, ignore...");
            return -1;
        }

        //上行消息序列号
        sdata[0] = msgba[1];
        sdata[1] = msgba[2];
        upSeq = Base64Utils.byteArrayToInt(sdata,2);

        //设备串号
        bytesToHexBuff(msgba, DEVOFFSET, 8, devbuf);
        deveui =devbuf.toString();

                //信号质量
        csq = (int)msgba[DEVOFFSET+8];

        //SIM卡CCID
        StringBuffer sb= new StringBuffer();
        for(int i=0; i<20; i++){
            sb.append(msgba[DEVOFFSET+9+i]&0xff);
        }
        ccid = sb.toString();

        //湿度
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            sdata[0] = msgba[DATAOFFSET];
            sdata[1] = msgba[DATAOFFSET+1];
            humi = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 5, "humi", (float) (humi*1.0/10)));
        }

        //温度
        sdata[0] = msgba[DATAOFFSET+2];
        sdata[1] = msgba[DATAOFFSET+3];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            temp = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 6, "temp", (float) (temp*1.0/10)));
        }

        //光照
        sdata[0] = msgba[DATAOFFSET+4];
        sdata[1] = msgba[DATAOFFSET+5];
        sdata[2] = msgba[DATAOFFSET+6];
        sdata[3] = msgba[DATAOFFSET+7];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff||sdata[2]!=(byte)0xff || sdata[3]!=(byte)0xff){
            illu = Base64Utils.byteArrayToInt(sdata,4);
            reports.add(new ReportData(1, 7, "illu", (float) (illu*1.0/1000)));
        }

        //气压
        sdata[0] = msgba[DATAOFFSET+8];
        sdata[1] = msgba[DATAOFFSET+9];
        sdata[2] = msgba[DATAOFFSET+10];
        sdata[3] = msgba[DATAOFFSET+11];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            airpress = Base64Utils.byteArrayToInt(sdata,4);
            reports.add(new ReportData(1, 21, "airPre", (float) (airpress*1.0/1000)));
        }

        //雨量
        sdata[0] = msgba[DATAOFFSET+12];
        sdata[1] = msgba[DATAOFFSET+13];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            rain = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 22, "rain15Min", (float) (rain*1.0/10)));
        }

        //风速
        sdata[0] = msgba[DATAOFFSET+14];
        sdata[1] = msgba[DATAOFFSET+15];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            windspd = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 23, "windSpd_jc", (float) (windspd*1.0/10)));
        }

        //风向
        sdata[0] = msgba[DATAOFFSET+16];
        sdata[1] = msgba[DATAOFFSET+17];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            winddrt = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 24, "windDrct_jc", (float) (winddrt*1.0)));
        }

        //土壤湿度
        sdata[0] = msgba[DATAOFFSET+18];
        sdata[1] = msgba[DATAOFFSET+19];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            soilhumi = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 25, "soilHumi", (float) (soilhumi*1.0/10)));
        }

        //土壤温度
        sdata[0] = msgba[DATAOFFSET+20];
        sdata[1] = msgba[DATAOFFSET+21];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            soiltemp = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 26, "soilTemp", (float) (soiltemp*1.0/10)));
        }

        //盐分
        sdata[0] = msgba[DATAOFFSET+22];
        sdata[1] = msgba[DATAOFFSET+23];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            soiltds = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 27, "soilTDS", (float) (soiltds*1.0/10)));
        }

        //ec
        sdata[0] = msgba[DATAOFFSET+24];
        sdata[1] = msgba[DATAOFFSET+25];
        if(sdata[0]!=(byte)0xff || sdata[1]!=(byte)0xff){
            soilec = Base64Utils.byteArrayToInt(sdata,2);
            reports.add(new ReportData(1, 28, "soilEC", (float) (soilec*1.0/10)));
        }
        //信号强度
        reports.add(new ReportData(1,29,"csq", (float) csq));

        if(msgba.length == GW4GSIZE){
            //电量
            batvolt = (int)msgba[DATAOFFSET+26]; //负数
            batvolt= batvolt & 0xFF; //byte转int后还是负数，需要转
            reports.add(new ReportData(1,30,"volt", (float) (batvolt*1.0/10)));
        }


//        log.info("收到EComGW deveui:{}, ccid:{}, csq:{}的周期上报消息." +
//                "humi:{}, temp:{}, illu:{}, ap:{}, rain:{}, wspd:{}, wdrt:{}, slhumi:{}, sltemp:{}, sltds:{}, slec:{} 处理成功!",
//                deveui, ccid, csq,
//                humi, temp,illu, airpress, rain, windspd, winddrt, soilhumi,soiltemp, soiltds, soilec);


        //生成省物联网平台上报结构体
        jsrpt.setDidKey("JIT4GWH"+deveui.toUpperCase() , "nx123123");
        jsrpt.setJCKVData((float)(temp*1.0/10), (float)(humi*1.0/10), (float) (airpress*1.0/1000),
                (float)(illu*1.0/1000), (float) (winddrt*22.5),  (float) (windspd*1.0/10), (float) (rain*1.0/10),
                (float) (soiltemp*1.0/10), (float) (soilhumi*1.0/10), (float) (soilec*1.0/10));

//        if(deveui.equalsIgnoreCase("8E02000213000240")){
//            makeCopyReport(jsrpt);
//        }
        return upSeq;
    }

    private void makeCopyReport(final JSPlatJCWHData rpt){
        final String devicePrefix = "JIT4GWH8E020002130002";
        log.info("Ecomm gw START MAKE COPY  weather msg to JSIOTPlat.");
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<40; i++){
                    String newdvid = devicePrefix+String.format("%02d", i+1);
                    JSPlatJCWHData copy = new JSPlatJCWHData(newdvid,rpt) ;
                    try {
                        Thread.sleep(20000);
//                        log.info("newdvid:{}, sleep finished, start post msg", newdvid);
                        boolean msgRptRes = httpCli.postMsg2JSPlat(copy);
                        if(msgRptRes){
                            log.info("Ecomm gw deveui:{} report weather msg to JSIOTPlat succeed, content:{}", newdvid, copy.toString());
                        }else{
                            log.error("Ecomm gw deveui:{} report weather msg to JSIOTPlat fail. content:{}", newdvid,copy.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();


                    }
                }

            }
        });
    }

}
