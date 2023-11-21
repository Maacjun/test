	package edu.jit.nsi.iot_ms.serviceimpl.impl;


    import edu.jit.nsi.iot_ms.commons.util.Base64Utils;
    import edu.jit.nsi.iot_ms.commons.util.JacksonUtils;
    import edu.jit.nsi.iot_ms.config.*;
    import edu.jit.nsi.iot_ms.domain.LoRaNwkDO;
    import edu.jit.nsi.iot_ms.mapper.LoRaNwkDAO;
    import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.JSIotJNDataModel;
    import edu.jit.nsi.iot_ms.serviceimpl.TermRspHandler;
    import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
    import edu.jit.nsi.iot_ms.session.PhySnrInfo;
    import edu.jit.nsi.iot_ms.session.SessionManager;
    import edu.jit.nsi.iot_ms.transport.ReportData;
    import edu.jit.nsi.iot_ms.transport.httpclient.HttpClientGate;
    import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.*;
    import edu.jit.nsi.iot_ms.transport.lora.LoRaMacUplink;
    import edu.jit.nsi.iot_ms.transport.lora.LoRaMsgDL;
    import edu.jit.nsi.iot_ms.transport.lora.MqttMessageSender;
    import edu.jit.nsi.iot_ms.transport.lora.RxInfo;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.extern.slf4j.Slf4j;
    import org.apache.commons.codec.binary.Base64;
    import org.apache.tomcat.util.buf.HexUtils;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;

    import java.util.*;
    import java.util.concurrent.ConcurrentHashMap;

    @Slf4j
    @Service
    public class LoraServiceImpl {
        private static int MAXS485 = 5;

        @Autowired
        SensorCmdCfg sensorCmdCfg;

        @Autowired
        MqttConfig mqttConfig;

        @Autowired
        TerminalServiceImpl terminalService;

        @Autowired
        SessionManager sessMngr;

        @Autowired
        IotServiceImpl iotService;

        @Autowired
        MqttMessageSender mqttMessageSender;

        @Autowired
        LoRaNwkDAO loRaNwkDAO;

        @Autowired
        JSIotJNDataModel jnmodel;

        @Autowired
        private HttpClientGate httpCli;

        private static final int SCAN_PER = 10000;
//        private Map<String, TermRspHandler> waitRspMap = new HashMap<>();  //等待APP响应
//        private Map<String,Long> dlStartMap = new HashMap<>();            //起始操作终端时间
        private ConcurrentHashMap<String, CtrlRsp> waitMap = new ConcurrentHashMap<>();  //等待APP响应
//        private int ctlmode = 1;   //0:by app , 1:by hand, 2:by computer

        private String appid = "";

        public void upLinkDecode(String msgJson){
            //数据包解析
            LoRaMacUplink uplink = JacksonUtils.readValue(msgJson, LoRaMacUplink.class);
            if(uplink==null){
                log.error("fail to decode josn foramt uplink msg: {}",msgJson);
                return;
            }
            appid = uplink.getApplicationID();
            String eui = uplink.getDevEUI();
            log.info("接受来自 {} 的信息长度为 {}", uplink.getDevEUI(), msgJson.length());

            //存储LoRa网络参数
            String addr = uplink.getDevAddr();
            boolean adr = uplink.getAdr();
            int cnt = uplink.getFCnt();
            int port = uplink.getFPort();

            int frq = uplink.getTxInfo().getFrequency();
            int dr = uplink.getTxInfo().getDr();

            Date now = new Date();
            if(uplink.getRxInfo()!=null ){
                boolean dupl = uplink.getRxInfo().size()>1 ? true: false;
                for(RxInfo rx : uplink.getRxInfo()){
                    loRaNwkDAO.insert(new LoRaNwkDO(eui, addr, frq, dr, adr, cnt, port, dupl,
                            rx.getGatewayID(), rx.getRssi(), rx.getLoRaSNR(), now));
                }
            }

            //检验终端devdui是否是有效分配的termid
            int termid = sessMngr.getTermIdByEUI(eui);
            if(termid == 0){
                termid = terminalService.getTermIdEui(eui);
                if(termid == 0){
                    log.error("get lora terminal deveui{} not exist.", eui);
                    return;
                }
            }

            //Payload内容 base64解码
            String base64String = uplink.getData();
            if(base64String==null){
                log.error("the data struct in deveui{} msg is null, ignore this msg", eui);
                return;
            }

            byte[] upmsg = Base64.decodeBase64(base64String);

            //根据不同的LoRa Application应用进入不同的业务处理
            String appname = uplink.getApplicationName();
            int termtype = 0;
            int dc = terminalService.getTermDataCycle(termid);
            int plat = terminalService.getTermToPlat(termid);
            if(uplink.getApplicationName().equals(mqttConfig.getAg21name())||
                    uplink.getApplicationName().equals(mqttConfig.getSmartname())){
                termtype=5;
                //添加到缓存中
                if(!sessMngr.isTermExist(termid)) {
                    sessMngr.putOneSess(termtype, termid, dc, plat,null, eui);
                }
                decodeAG21(termid, eui, upmsg);
            }else if(appname.equals(mqttConfig.getPlantAppName())){
                termtype=2;
                //添加到缓存中
                if(!sessMngr.isTermExist(termid)) {
                    sessMngr.putOneSess(termtype, termid, dc,plat,null, eui);
                }
                decodeSeeed(termid,upmsg);
            }else if(appname.equals(mqttConfig.getWthrAppName())||
                    appname.equals(mqttConfig.getAquaAppName())||
                    appname.equals(mqttConfig.getAtname())){
                termtype=4;
                //添加到缓存中
                if(!sessMngr.isTermExist(termid)) {
                    sessMngr.putOneSess(termtype, termid, dc,plat,null, eui);
                }
                decodeEasyComm(termid,upmsg);
            }else if(appname.equals(mqttConfig.getAQCtrlname())){
                termtype=4;
                //添加到缓存中
                if(!sessMngr.isTermExist(termid)) {
                    sessMngr.putOneSess(termtype, termid, dc,plat,null, eui);
                }
                byte cmd = upmsg[3];
                if(cmd==(byte)0x01) {
                    decodeEasyComm(termid,upmsg);
                }
                else if(cmd==(byte)0xe1) {
                    upLinkCtrlRsp(eui, termid, upmsg);
                }
            }
            log.info("deveui:{}, app:{}, termid:{} 上报消息处理成功!", eui, appname, termid);
        }



        /**
         * @Description 解码矽递终端Payload
         **/
        private void decodeSeeed(int termid, byte[] data){
            List<ReportData> rptlist = new ArrayList<>();

            //base64解码并开始解析
            Date now = new Date();
    //        log.info("data:{}", HexUtils.toHexString(data));
            int dataLength = data.length;
            int typeNum = dataLength / 7;
            for (int i = 0; i < typeNum; i++) {
                //解析 类型和值
                byte typeArray[] = new byte[2];
                typeArray[0] = data[2 + i * 7];
                typeArray[1] = data[1 + i * 7];
                int typeInt = Base64Utils.byteArrayToInt(typeArray,2);
                String type0 = HexUtils.toHexString(typeArray);
                String type = sensorCmdCfg.getSeeedFrameType(typeInt);
                if (null == type) {
                    log.error("product(hex):{} illegal.", HexUtils.toHexString(data));
                    continue;
                }

                //目前只存储传感器数据(10**)和电池电量(0007)
                if(type.equals("btyPer")){
                    byte voltArray[] = new byte[2];
                    //电池电量百分比（1~100%）
                    voltArray[0] = data[4 + i * 7];
                    voltArray[1] = data[3 + i * 7];
                    int voltPer = Base64Utils.byteArrayToInt(voltArray,2);
                    //电池电量上报周期（分钟）
                    voltArray[0] = data[4 + i * 7];
                    voltArray[1] = data[3 + i * 7];
                    int voltPro = Base64Utils.byteArrayToInt(voltArray,2);
                    rptlist.add(new ReportData(1, typeInt, type, voltPer));
                }else if(type0.startsWith("10")){
                    byte valueArray[] = new byte[4];
                    valueArray[0] = data[6 + i * 7];
                    valueArray[1] = data[5 + i * 7];
                    valueArray[2] = data[4 + i * 7];
                    valueArray[3] = data[3 + i * 7];
                    int valueInt = Base64Utils.byteArrayToInt(valueArray, 4);
                    int unit = sensorCmdCfg.getSeeedFrameUnion(typeInt);
                    float value_f = (float)0.0;
                    if(type.equals("temp")){
                        value_f = (float) (((short)valueInt) / (unit*1.0));
                    }else{
                        value_f = (float) (valueInt / (unit*1.0));
                    }
                    rptlist.add(new ReportData(1, typeInt, type, value_f));
                }
            }

            //插入数据库
            iotService.recordReport(termid, rptlist, 1);
        }

        /**
         * @Description 解码易通终端Payload
         **/
        public void decodeEasyComm(int termid, byte[] data){
            List<ReportData> rptlist = new ArrayList<>();

            int idx = 0;
            if(data[idx++]!= (byte)0xfe){  //idx:0-->1
                log.error("easycomm msg start delimiter {} != 0xfe", data[0]);
                return;
            }
            byte msglen = data[idx++];  //idx:1-->2
            byte curone = 0;
            byte bchk2 = 0;
            byte ver = data[idx++]; //idx:2-->3
            byte cmd = data[idx++];  //idx:3-->4
            bchk2 += msglen;
            bchk2 += ver;
            bchk2 += cmd;

            while(idx<msglen-2){
                boolean isvalid=false;
                byte btype = data[idx++];
                byte len = data[idx++];
                bchk2 += btype;
                bchk2 += len;
                byte[] va_arr = new byte[len];
                for(int j=len-1;j>=0; j--) {
                    curone  = data[idx++];
                    va_arr[j] = curone;
                    bchk2 += curone;
                    if(curone != (byte)0xff)
                        isvalid = true;
                }
                //当前参数数值无效则忽略当前参数
                if(!isvalid){
                    continue;
                }
                //获得type
                String type = sensorCmdCfg.getEasycommFrameType(btype);
                int unit =sensorCmdCfg.getEasycommFrameUnit(btype);
                if(type==null)
                    type = "unknow";
                //获得value
                int value_i = Base64Utils.byteArrayToInt(va_arr, len);
                float value_f = (float)0.0;
                if(len<=4){
//                    if(devEUI.toLowerCase().equals("8d00000100000005")&&type.equals("ph")){
//                        value_f = (float) ( value_i/ (100*1.0) );
//                    }else {
                        value_f = (float) (value_i / (unit*1.0) );
//                    }

                    if(type.equals("temp")){

                        value_f = (float) (((short)value_i) / (unit*1.0) );
                    }else{
                        value_f = (float) (value_i / (unit*1.0) );
                    }
                }

                rptlist.add(new ReportData(1, (int)btype, type, value_f));
            }

            if(bchk2 != data[idx++]){
                log.error("easycomm msg check val error, bchk2:{} != data:{}",bchk2, data[idx-1]);
            }
            if(data[idx]!=(byte)0xfe){
                log.error("easycomm msg end delimiter {} != 0xfe", data[msglen-1]);
            }

            //插入数据库
            iotService.recordReport(termid, rptlist, 1);
            int toplat = sessMngr.getTermPlat(termid);
            ////蓝莓园的气象数据上报省物联网平台  termid == 108
            if(toplat==1){
                float temp=0.0f, humi=0.0f, illu=0.0f, spd=0.0f, dir=0.0f, rain=0.0f;
                for(ReportData rptdata : rptlist){
                    switch (rptdata.getType()){
                        case "temp":
                            temp = rptdata.getValue();
                        case "humi":
                            humi = rptdata.getValue();
                        case "illu":
                            illu = rptdata.getValue();
                        case "windSpd":
                            spd = rptdata.getValue();
                        case "windDrct":
                            dir = rptdata.getValue();
                        case "rain":
                            rain = rptdata.getValue();
                    }

                }
                JSPlatJNSession jsrpt= new JSPlatHKWHData("JITLoRaWH"+termid, "121345", temp, humi, illu, spd, dir, rain);
                boolean msgRptRes = httpCli.postMsg2JSPlat(jsrpt);
                if(msgRptRes){
                    log.info("LD10 id:{} report WH msg to JSIOTPlat succeed.", termid);
                }else{
                    log.error("LD10 id:{} report WH msg to JSIOTPlat fail.", termid);
                }
            }

        }

        /**
         * @Description AG21终端Payload消息类型判断
         **/
        public void decodeAG21(int termid, String eui, byte[] data){
            log.info("decode ag21 term id:{}, eui:{}", termid, eui);
            byte bchk2 = 0;  //校验码
            if(data[0]!= (byte)0xfe){  //idx:0-->1
                log.error("easycomm msg start delimiter {} != 0xfe", data[0]);
                return;
            }
            byte msglen = data[1];  //idx:1-->2
            if(data[msglen-1]!= (byte)0xfe){  //idx:0-->1
                log.error("easycomm msg end delimiter {} != 0xfe", data[1+msglen+2]);
                return;
            }

            byte type = data[3];  //idx:3-->4
            switch (type){
                case 0x01:
                    //返回注册请求响应携带485指令
                    ag21Register(termid, eui, data);
                    break;
                case 0x03:
                    //上报消息
                    ag21Report(termid, eui, data);
                    break;
                case 0x04:
                    //上报电池电量
                    ag21Btry(termid, data);
                    break;

                case 0x11:
                case 0x12:
                case 0x13:
                    //控制响应
                    upLinkCtrlRsp(eui, termid, data);
                    break;
                default:
                    break;
            }
            return;
        }

        /**
         * @Description AG21终端注册消息
         **/
        private void ag21Register(int tid, String eui, byte[] data){
            byte bchk = 0;  //校验码
            int idx = 1;

            byte msglen = data[idx++];  //idx:1-->2
            byte ver = data[idx++]; //idx:2-->3
            byte type = data[idx++];  //idx:3-->4
            bchk += msglen;
            bchk += ver;
            bchk += type;
            if(bchk != data[idx++]){
                log.error("easycomm_ag21 msg check val error, bchk2:{} != data:{}",bchk, data[idx-1]);
            }
            //收到注册请求
            List<SensorCmd> sensors = new ArrayList<>();
            List<RelayCtlCmd> relays = new ArrayList<>();
            iotService.termSRList(tid, sensors, relays);
            //缓存记录
            sessMngr.ag21TermPhyInfo(tid,sensors);
            sendRegisterRsp(tid, eui, sensors);
            log.info("ag21 send eui:{} register rsp msg ", eui);
        }

        /**
         * @Description AG21终端采集周期上报消息
         **/
        private void ag21Report(int tid, String deveui, byte[] data) {
            byte bchk = 0;  //校验码
            int idx = 1;
            byte curone = 0;
            List<ReportData> rptlist = new ArrayList<>();
            byte msglen = data[idx++];  //idx:1-->2
            byte ver = data[idx++]; //idx:2-->3
            byte type = data[idx++];  //idx:3-->4
            bchk += msglen;
            bchk += ver;
            bchk += type;

            //隐式上线
            if (!sessMngr.isAG21PhyInfo(tid)) {
                log.info("easycomm_ag21:{} implicit register", tid);
                //收到注册请求
                List<SensorCmd> sensors = new ArrayList<>();
                List<RelayCtlCmd> relays = new ArrayList<>();
                iotService.termSRList(tid, sensors, relays);
                //缓存记录
                sessMngr.ag21TermPhyInfo(tid, sensors);
            }

            while (idx < msglen - 2) {
                byte addr = data[idx++];
                byte len = data[idx++];
                bchk += addr;
                bchk += len;
                //处理一个物理传感器上报的一组参数
                byte[] va_arr = new byte[len];
                for (int j = 0; j < len && idx < msglen - 2; j++) {
                    curone = data[idx++];
                    va_arr[j] = curone;
                    bchk += curone;
                }

                //获取缓存终端信息中的物理传感器信息
                PhySnrInfo phyInfo = sessMngr.getTermPhyInfo(tid, addr);
                if (phyInfo == null) {
                    log.error("easycomm_ag21:{} reprot msg, addr:{} not config, ignore..", tid, addr);
                    return;
                }

                int reg_start = phyInfo.getReg();
                if (phyInfo.getProduct().startsWith("RELAY_") &&
                        ((phyInfo.getLen()>=8&&phyInfo.getLen()/8==len) ||(phyInfo.getLen()==4&&phyInfo.getLen()/4==len))){
                    log.info("easycomm_ag21:{} reprot msg is relay(addr={}, roadnum={}) status.", tid, addr, phyInfo.getLen());
                } else if (len != phyInfo.getLen() * 2) {
                    log.error("easycomm_ag21:{} reprot msg, 485cmd:{} reg:{} len:{} != info.getReg:{}*2, continue next sensor data..",
                            tid, addr, reg_start, len, phyInfo.getLen());
                    continue;
                }

                //加入当前phy sensor中的report value.
                //处理一个物理传感器上报的数据
                List<ReportData> rlist = decodePhyValue(addr, phyInfo, va_arr, len);
                //log.info("ag21 id:{} decode value get param size:{}.", tid, rlist.size());
                rptlist.addAll(rlist);

//                for(int i=0; i<len; i+=2){
//                    vaArray[0]=va_arr[i*2];
//                    vaArray[1]=va_arr[i*2+1];
//                    int value = Base64Utils.byteArrayToInt(vaArray,2);
//                    for(SensorValue sv:phyInfo.getRspvalue()){
//                        if(sv.getValue().get(0) == 3 +i/2) { //485响应于第三个字节
//                            float value_f = (float) (value / (sv.getUnit()*1.0) );
//                            //增加
//                            rptlist.add(new ReportData(addr, (int)reg_start+i/2, sv.getStype(), value_f));
//                        }
//                    }
//                }
            }
            if (bchk != data[idx++]) {
                log.error("easycomm_ag21 msg check val error, bchk2:{} != data:{}", bchk, data[idx - 1]);
            }

            //插入数据库
            iotService.recordReport(tid, rptlist, 1);
            log.info("ag21 id:{} report msg param_size:{} into db.", tid, rptlist.size());
            int toplat = sessMngr.getTermPlat(tid);

            if (toplat==1) {
                /**蓝莓园的数据土壤墒情数据上报省物联网平台  tid >= 110 && tid <= 124**/
                JSPlatJNSession jsrpt=null;   //上报省平台通用消息格式
                if(tid >= 110 && tid <= 124){
                    float temp = 0.0f, humi = 0.0f, ec = 0.0f;
                    for (ReportData rptdata : rptlist) {
                        switch (rptdata.getType()) {
                            case "soilTemp":
                                temp = rptdata.getValue();
                            case "soilHumi":
                                humi = rptdata.getValue();
                            case "soilEC":
                                ec = rptdata.getValue();
                        }
                    }
                    jsrpt = new JSPlatHKSoilData("JITLoRaSoil" + tid /*"njpk01"*/, "121345", temp, humi, ec / 10000);
                }
                /**园艺站项目扩增上报省物联网平台**/
                for (ReportData rptdata : rptlist){
                    jnmodel.updtModel(tid, rptdata);
                }

                //温湿度
//                else if (tid >= 131 && tid <= 140) {
//                    float temp = 0.0f, humi = 0.0f;
//                    for (ReportData rptdata : rptlist) {
//                        switch (rptdata.getType()) {
//                            case "temp":
//                                temp = rptdata.getValue();
//                            case "humi":
//                                humi = rptdata.getValue();
//                        }
//                    }
//                    jsrpt = new JSPlatJCAirTHData("JITLoRaGH" + deveui.toUpperCase(), "nx123123", temp, humi);
//                } else if (tid >= 141 && tid <= 160) {
//                    //光照
//                    float illu = 0.0f;
//                    for (ReportData rptdata : rptlist) {
//                        switch (rptdata.getType()) {
//                            case "illu":
//                                illu = rptdata.getValue();
//                        }
//                    }
//                    jsrpt = new JSPlatJCIlluData("JITLoRaGH" + deveui.toUpperCase(), "nx123123", illu);
//                } else if (tid >= 161 && tid <= 170) {
//                    //光合作用
//                    float rad = 0.0f;
//                    for (ReportData rptdata : rptlist) {
//                        switch (rptdata.getType()) {
//                            case "rad":
//                                rad = rptdata.getValue();
//                        }
//                    }
//                    jsrpt = new JSPlatJCRadData("JITLoRaGH" + deveui.toUpperCase(), "nx123123", rad);
//                } else if (tid >= 171 && tid <= 190) {
//                    //CO2
//                    float co2 = 0.0f;
//                    for (ReportData rptdata : rptlist) {
//                        switch (rptdata.getType()) {
//                            case "co2":
//                                co2 = rptdata.getValue();
//                        }
//                    }
//                    jsrpt = new JSPlatJCCo2Data("JITLoRaGH" + deveui.toUpperCase(), "nx123123", co2);
//                } else if (tid >= 193 && tid <= 222) {
//                    //土壤四合一
//                    float stemp = 0.0f, shumi = 0.0f, salt = 0.0f, ec = 0.0f;
//                    for (ReportData rptdata : rptlist) {
//                        switch (rptdata.getType()) {
//                            case "soilTemp":
//                                stemp = rptdata.getValue();
//                            case "soilHumi":
//                                shumi = rptdata.getValue();
//                            case "soilTDS":
//                                salt = rptdata.getValue();
//                            case "soilEC":
//                                ec = rptdata.getValue();
//
//                        }
//                    }
//                    jsrpt = new JSPlatJCSoilData("JITLoRaGH" + deveui.toUpperCase(), "nx123123", stemp, shumi, salt, ec / 10000);
//                }
//                boolean msgRptRes = httpCli.postMsg2JSPlat(jsrpt);
//                sessMngr.updateTermNum(tid,msgRptRes);
//                if (msgRptRes) {
//                    log.info("AG21 deveui:{} report Soil/GreenHouse msg to JSIOTPlat succeed.", deveui);
//                } else {
//                    log.error("AG21 deveui:{} report Soil/GreenHouse msg to JSIOTPlat fail.", deveui);
//                }
            }

        }

        private List<ReportData> decodePhyValue(int addr, PhySnrInfo phyInfo, byte[] va_arr, int len){
            List<ReportData> phyrptlist = new ArrayList<>();
            byte vaArray[] = new byte[4];
            int reg_start = phyInfo.getReg();
            //解析485响应数据区中当前传感器的返回值
            int i=0;
            boolean regmatch = false;
            while(i<len){
                for(SensorValue sv:phyInfo.getRspvalue()){
                    if(sv.getValue().get(0) == i+3){
                        int va_size = sv.getValue().size();
                        int reg = reg_start+i/2;
                        //计算reg的value
                        for(int j=0; j<va_size; j++)
                            vaArray[j] = va_arr[i++];
                        int value = Base64Utils.byteArrayToInt(vaArray,va_size);
                        float value_f = (float) (value / (sv.getUnit()*1.0) );
                        //构造并增加当前寄存器对象
                        phyrptlist.add(new ReportData(addr, reg, sv.getStype(), value_f));
                        //结束本次循环
                        regmatch=true;
                        break;
                    }
                }
                //在当前reg不需要解析（不存在于def_sensor_type）的情况下
                if(!regmatch){
                    i++;
                }
            }
            return phyrptlist;
        }

        /**
         * @Description AG21终端电池电压消息
         **/
        private void ag21Btry(int tid, byte[] data){
            byte bchk = 0;  //校验码
            int idx = 1;
            byte curone = 0;
            List<ReportData> rptlist = new ArrayList<>();

            byte msglen = data[idx++];  //idx:1-->2
            byte ver = data[idx++]; //idx:2-->3
            byte type = data[idx++];  //idx:3-->4
            bchk += msglen;
            bchk += ver;
            bchk += type;

            //电池电压
            if(msglen != 10){
                log.error("easycomm_ag21 termid:{} battry volt msg len:{} != 10 ",tid, msglen);
                return;
            }
            byte addr = data[idx++];
            byte len = data[idx++];
            bchk += addr;
            bchk += len;

            byte vaArray[] = new byte[2];
            vaArray[0]=data[idx++];
            vaArray[1]=data[idx++];
            int value = Base64Utils.byteArrayToInt(vaArray,2);
            float value_f = (float) (value / (10*1.0) );

            rptlist.add(new ReportData(1, 0, "volt", value_f));

            if(bchk != data[idx++]){
                log.error("easycomm_ag21 msg check val error, bchk2{} != data{}",bchk, data[idx-1]);
            }

            //插入数据库
            iotService.recordReport(tid, rptlist, 1);
            log.info("ag21 id:{} battery volt msg into db.", tid);
        }


        /**
         * @Description AG21终端注册响应
         * 返回传感器485指令集
         **/
        private void sendRegisterRsp(int tid, String deveui, List<SensorCmd>sensors){
            int snum=0;
//            int []scmd_len = new int[MAXS485];
            byte[][] cmd485 = new byte[MAXS485][6];
            for(int i=0; i<sensors.size()&&snum<MAXS485; i++){
                SensorCmd s = sensors.get(i);
                List<Integer> addrs = s.getAddr();
                int fcode = s.getFcode();
                int reg = (Integer)s.getReg().get(0); //不考虑多个寄存器的情况
                int len = s.getLen();

                for(int j=0; j<addrs.size()&&snum<MAXS485; j++){
                    int ad = addrs.get(j);
//                    scmd_len[snum] = 6;
                    cmd485[snum][0] = (byte)ad;
                    cmd485[snum][1] = (byte)fcode;
                    cmd485[snum][2] = (byte) ((reg>>8)&0xff);
                    cmd485[snum][3] = (byte) (reg&0xff);
                    cmd485[snum][4] = (byte) ((len>>8)&0xff);
                    cmd485[snum][5] = (byte) (len&0xff);
                    snum++;
                }
            }

            downLink485(deveui, terminalService.getTermDataCycle(tid), terminalService.getTermPreHeat(tid), snum,  /*scmd_len,*/ cmd485);
        }

        /**
         * server端的终端配置的485传感器信息
         * @param deveui    eui
         * @param cmd485    485命令数组
         */
        public boolean downLink485(String deveui, int dc, int preh, int snrnum, byte [][]cmd485){
            //构造下发命令  FE ** 01 E2 01 01 01 ** EF --> 打开addr 01第一路 01打开
            /** 0	0xFE	起始符
             *   1	length	总长度
             *   2	0X02	版本号
             *   3	Type	命令码(0X2)
             *   4	DataCycl（H）	休眠时长高字节（秒）
             *   5	DataCycle（L）	休眠时长低字节（秒）
             *   6	PreHeat（H）	预热时长高字节（秒）
             *   7	PreHeat（L）	预热时长低字节（秒）
             *   ...
             *   m   addr  1个byte
             *   m+1 len   1个byte
             *   m+5 addr  5个byte
             *   ...
             *   8 check_val
             *   9 0xFE	结束符    **/
            int dMsgSize = 10+snrnum*(1+1+5);
            if(dMsgSize>50){
                log.error("easycomm downlink msg len{} too long, ignore!", dMsgSize);
                return false;
            }

            byte[] ctrlBytes = new byte[dMsgSize];
            byte bchk2 = 0;
            int idx = 0;
            ctrlBytes[idx++] = (byte) 0xfe;
            ctrlBytes[idx++] = (byte) dMsgSize;
            ctrlBytes[idx++] = (byte) 0x02;  //Ver
            ctrlBytes[idx++] = (byte) 0x02;  //Type
            ctrlBytes[idx++] = (byte) ((dc>>8)&0xff);  //report interval HI
            ctrlBytes[idx++] = (byte) (dc&0xff);  //report interval LOW
            ctrlBytes[idx++] = (byte) ((preh>>8)&0xff);   //sensor preheat HI
            ctrlBytes[idx++] = (byte) (preh&0xff);  //sensor preheat LOW

            for(int i=0; i<snrnum; i++){
                ctrlBytes[idx++] = (byte) cmd485[i][0];  //Addr
                ctrlBytes[idx++] = (byte) 0x05;  //len固定=5
                for(int j=1; j<6; j++) {
                    ctrlBytes[idx++] = (byte) cmd485[i][j];
                }
            }

            ctrlBytes[idx++] = (byte) 0x00;
            ctrlBytes[idx++] = (byte) 0xfe;

            if(idx !=dMsgSize){
                log.error("easycomm downlink msg cur position idx:{} and len:{} not match!", idx, dMsgSize);
            }

            //计算更新校验码
            for(int i=1; i<dMsgSize-2; i++)
                bchk2 += ctrlBytes[i];
            ctrlBytes[dMsgSize-2] = (byte) bchk2;

            String ctrlbody = Base64.encodeBase64String(ctrlBytes);
            LoRaMsgDL dlmsg = new LoRaMsgDL(false, 1, ctrlbody);
            mqttMessageSender.send(deveui, appid, JacksonUtils.toJson(dlmsg));
            log.info("send downlink register rsp to deveui{}, snrnum:{}, size:{}", deveui, snrnum, dMsgSize);
            return true;

        }

        /**
         * server端的查询/控制透传功能，将query与control消息的请求与响应在APP与终端之间透传
         *  comm消息应该收不到了
         * @param clm       控制类型
         * @param deveui    消息对象
         * @param addr      会话
         * @param road      消息对象
         * @param onoff     会话
         * @param rsphandler 消息对象
         */
        public boolean downLinkCtrl(int clm, String deveui, int addr, int road, int onoff, TermRspHandler rsphandler){

            //检查是否有对该
            if(waitMap.containsKey(deveui)){
                log.error("there's another downlink msg in the waiting queue, ignore this one in timeout.");
                rsphandler.timeout();
                return false;
            }
            waitMap.put(deveui, new CtrlRsp(addr, road, onoff, clm, System.currentTimeMillis(),rsphandler));

            //构造下发命令  FE 09 02 10 01 01 01 ** EF --> 打开addr 01第一路 01打开
            byte[] ctrlBytes = new byte[9];
            byte bchk2 = 0;
            ctrlBytes[0] = (byte) 0xfe;
            ctrlBytes[1] = (byte) 0x09;
            ctrlBytes[2] = (byte) 0x02;
            ctrlBytes[3] = (byte) 0x10;
            ctrlBytes[4] = (byte) addr;
            ctrlBytes[5] = (byte) road;
            ctrlBytes[6] = (byte) onoff;
            for(int i=1; i<=6; i++)
                bchk2 += ctrlBytes[i];
            ctrlBytes[7] = (byte) bchk2;
            ctrlBytes[8] = (byte) 0xfe;

            String ctrlbody = Base64.encodeBase64String(ctrlBytes);

            LoRaMsgDL dlmsg = new LoRaMsgDL(false, 1, ctrlbody);
            mqttMessageSender.send(deveui, "5", JacksonUtils.toJson(dlmsg));
            log.info("send downlink ctrl to appid:{} deveui:{} addr:{}, road:{}, opt:{}", 7,deveui, addr, road, onoff);
            return true;
        }

        //返回成功响应                         FE 09 02 11 01 01 F0 ** FE
        //返回超时响应（打开超时,状态00 00）     FE 0a 02 12 02 02 00 00 ** FE
        //返回控制失败响应（打开失败,状态00 00）  FE 0a 02 13 02 02 00 00 ** FE
        private void upLinkCtrlRsp(String deveui, int termid, byte[] data){
            List<ReportData> rptlist = new ArrayList<>();
            if(data[0]!= (byte)0xfe){  //idx:0-->1
                log.error("easycomm msg start delimiter {} != 0xfe", data[0]);
                return;
            }

            boolean sucdfg = false;
            byte retcode = data[3];
            byte addr = data[4];
            byte va_size = data[5];
            byte[] va_arr = new byte[va_size];
            for(int i=0;i<va_size; i++){
                va_arr[i] = data[6+i];
            }

            //获取缓存终端信息中的物理传感器信息
            PhySnrInfo phyInfo= sessMngr.getTermPhyInfo(termid, addr);
            if(phyInfo==null){
                log.error("easycomm_ag21:{} reprot msg, addr:{} not config, ignore..", termid, addr);
            }

            int status = Base64Utils.byteArrayToInt(va_arr,va_size);
            rptlist.add(new ReportData(addr, 0, "relay_sta", status));
            //插入数据库
            iotService.recordReport(termid, rptlist, 1);
            log.info("recv uplink ctrl_rsp from deveui{} , ret:{}, status:{}, report relay status into db.", deveui, retcode, status);

            if(!waitMap.containsKey(deveui)){
                log.error("recv ctrl_rsp,  but deveui{} not exist in waitMap!!!", deveui);
                return;
            }
            CtrlRsp ctrlRsp = waitMap.get(deveui);
            TermRspHandler rsphandler = ctrlRsp.getHandler();

            if(retcode==0x11){
                sessMngr.updateEquipCtl(termid, ctrlRsp.getAddr(), ctrlRsp.getRoad(), ctrlRsp.getOpt(), ctrlRsp.getCtlmode());
                log.info("save termid:{}, addr:{}, road:{}, opt:{}, ctlmode:{} into db.", termid, ctrlRsp.getAddr(), ctrlRsp.getRoad(), ctrlRsp.getOpt(), ctrlRsp.getCtlmode());
                sucdfg = true;
            }

            //必须先remove在执行回调
            if(rsphandler!=null){
                if(sucdfg)
                    rsphandler.execute();
                else
                    rsphandler.timeout();
            }

            waitMap.remove(deveui);
        }

        /**
         * 等待响应超时
         * @param deveui 终端串号
         */
        void onTimeOut(String deveui){
            CtrlRsp ctrlRsp = waitMap.get(deveui);
            TermRspHandler rsphandler = ctrlRsp.getHandler();
            waitMap.remove(deveui);
            log.info("处理deveui为{}的终端等待终端响应超时, 剩余map size={}，执行回调", deveui, waitMap.size());
            if(rsphandler!=null){
                rsphandler.timeout();
            }
        }

        /**
         * 周期扫描Map中的等待响应缓存
         */
        @Scheduled(fixedDelay = SCAN_PER)
        private void periodScanMap() {
            Set<String> deveuis = waitMap.keySet();
            for (String eui : deveuis) {
                long start = waitMap.get(eui).getStarttm();
                long now = System.currentTimeMillis();
                //等待iot终端响应超时，需要删除map中的记录
                if (now - start > SCAN_PER) {
                    onTimeOut(eui);
                }
            }
        }

        //添加新值
        private void updateInfluxDB(String measurement, Object value) {
    //        Map<String, String> tag = new HashMap<>();
    //        Map<String, Object> field = new HashMap<>();
    //        field.put("valueMap", valueMap.toString());
    //
    //        JSONObject tags = JSONObject.fromObject(tag);
    //        JSONObject fields = JSONObject.fromObject(field);
    //        dataDao.insert(measurement, tags.toString(), fields.toString());
            log.debug("influxdb : save to {},valueMap={}", measurement, value);
        }


        @Data
        @AllArgsConstructor
        private class CtrlRsp{
            int addr;
            int road;
            int opt;
            int ctlmode;    //0:by app , 1:by hand, 2:by computer
            long starttm;
            TermRspHandler handler;
        }
    }
