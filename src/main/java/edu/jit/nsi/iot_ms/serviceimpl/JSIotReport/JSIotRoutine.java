package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.jit.nsi.iot_ms.commons.util.JacksonUtils;
import edu.jit.nsi.iot_ms.domain.JSPlatJNDevDO;
import edu.jit.nsi.iot_ms.mapper.JNDevStatDAO;
import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023.*;
import edu.jit.nsi.iot_ms.transport.httpclient.HttpClientGate;
import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class JSIotRoutine {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    JSIotJNDataModel jnmodel;

    @Autowired
    private HttpClientGate httpCli;

    @Autowired
    JNDevStatDAO jnDevStatDAO;

    private final static int SCANSEC = 30;
    Map<String, JSPlatJNDevDO> JnDevicesMap = new HashMap<>();
    List<JSIotDvcDef<JNAgriOrg>> deviceDefLst;
    //读配置文件初始化DeviceList
    private int cycnt = 0;

    public JSIotRoutine(){
        initMdlMatchFromFile();
        initDeviceMap();
    }

    public List<String> getMachemdl(int tid){
        List<String> modlst = new ArrayList<>();
        for(JSIotDvcDef model:deviceDefLst){
            if(model.getTermidlst().contains(tid)) {
                log.info("JS IOT 2023 recv match termid:{} in {}.", tid, model.getGroupname());
                modlst.add(model.getGroupname());
            }
        }
        return modlst;
    }

    @Scheduled(fixedDelay = SCANSEC * 1000)
    private synchronized void periodTimeScan() {
        boolean saveDBfg = false;
        //2小时保存数据库一次
        if (cycnt++ >= 60 *60 / SCANSEC * 2) {
            saveDBfg=true;
            cycnt=0;
        }
        log.info("JS IOT 2023 {}'st period scan start.", cycnt);
        Date now =new Date();
        for(JSIotDvcDef<JNAgriOrg> model:deviceDefLst){
            for(JNAgriOrg org : model.getOrglst()){
                for(String dvname : org.getDeviceidlst()){
                    if(JnDevicesMap.containsKey(dvname)){
                        JSPlatJNDevDO dev = JnDevicesMap.get(dvname);
                        //判断超过上报时长即可上报数据
                        if(dev.isAction()){
                            log.info("[debug] JS IOT 2023 model:{}, device:{} is reporting msg.", model.getGroupname(), dvname);
                            switch (model.getGroupname()){
                                case "ghair4":
                                    Air4 air4 = jnmodel.constructAir4(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAir4 = new JSPlatGHAir4(dev.getDeviceid(),"121345", air4);
                                    log.info("[debug]JS IOT 2023 report Air4 msg to JSIOTPlat： {}.", ((JSPlatGHAir4)jsPlatGHAir4).getData());
                                    boolean rspOK = httpCli.postMsg2JSPlat(jsPlatGHAir4);
                                    dev.incrMsgNum(rspOK);
                                    dev.incrMsgPeriod(rspOK);
                                    if (rspOK) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil4_1":
                                    AirSoil4_1 as4_1 = jnmodel.constructAS4_1(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS4_1 = new JSPlatGHAirSoil4_1(dev.getDeviceid(),"121345", as4_1);
                                    log.info("[debug]JS IOT 2023 report AirSoil4_1 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil4_1) jsPlatGHAS4_1).getData());
                                    boolean rspOK0 = httpCli.postMsg2JSPlat(jsPlatGHAS4_1);
                                    dev.incrMsgNum(rspOK0);
                                    dev.incrMsgPeriod(rspOK0);
                                    if (rspOK0) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil4_2":
                                    AirSoil4_2 as4_2 = jnmodel.constructAS4_2(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS4_2 = new JSPlatGHAirSoil4_2(dev.getDeviceid(),"121345", as4_2);
                                    log.info("[debug]JS IOT 2023 report AirSoil4_2 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil4_2) jsPlatGHAS4_2).getData());
                                    boolean rspOK1 = httpCli.postMsg2JSPlat(jsPlatGHAS4_2);
                                    dev.incrMsgNum(rspOK1);
                                    dev.incrMsgPeriod(rspOK1);
                                    if (rspOK1) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil4_3":
                                    AirSoil4_3 as4_3 = jnmodel.constructAS4_3(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS4_3 = new JSPlatGHAirSoil4_3(dev.getDeviceid(),"121345", as4_3);
                                    log.info("[debug]JS IOT 2023 report AirSoil4_3 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil4_3) jsPlatGHAS4_3).getData());
                                    boolean rspOK1_1 = httpCli.postMsg2JSPlat(jsPlatGHAS4_3);
                                    dev.incrMsgNum(rspOK1_1);
                                    dev.incrMsgPeriod(rspOK1_1);
                                    if (rspOK1_1) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil5":
                                    AirSoil5 as5 = jnmodel.constructAS5(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS5 = new JSPlatGHAirSoil5(dev.getDeviceid(),"121345", as5);
                                    log.info("[debug]JS IOT 2023 report AirSoil5 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil5) jsPlatGHAS5).getData());
                                    boolean rspOK2 = httpCli.postMsg2JSPlat(jsPlatGHAS5);
                                    dev.incrMsgNum(rspOK2);
                                    dev.incrMsgPeriod(rspOK2);
                                    if (rspOK2) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil5_2":
                                    AirSoil5 as5_2 = jnmodel.constructAS5(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS5_2 = new JSPlatGHAirSoil5(dev.getDeviceid(),"121345", as5_2);
                                    log.info("[debug]JS IOT 2023 report AirSoil5 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil5) jsPlatGHAS5_2).getData());
                                    boolean rspOK3 = httpCli.postMsg2JSPlat(jsPlatGHAS5_2);
                                    dev.incrMsgNum(rspOK3);
                                    dev.incrMsgPeriod(rspOK3);
                                    if (rspOK3) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil6_1":
                                    AirSoil6_1 as6_1 = jnmodel.constructAS6_1(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS6_1 = new JSPlatGHAirSoil6_1(dev.getDeviceid(),"121345", as6_1);
                                    log.info("[debug]JS IOT 2023 report AirSoil6_1 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil6_1) jsPlatGHAS6_1).getData());
                                    boolean rspOK4 = httpCli.postMsg2JSPlat(jsPlatGHAS6_1);
                                    dev.incrMsgNum(rspOK4);
                                    dev.incrMsgPeriod(rspOK4);
                                    if (rspOK4) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil6_2":
                                    AirSoil6_2 as6_2 = jnmodel.constructAS6_2(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS6_2 = new JSPlatGHAirSoil6_2(dev.getDeviceid(),"121345", as6_2);
                                    log.info("[debug]JS IOT 2023 report AirSoil6_2 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil6_2) jsPlatGHAS6_2).getData());
                                    boolean rspOK5 = httpCli.postMsg2JSPlat(jsPlatGHAS6_2);
                                    dev.incrMsgNum(rspOK5);
                                    dev.incrMsgPeriod(rspOK5);
                                    if (rspOK5) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil6_3":
                                    AirSoil6_3 as6_3 = jnmodel.constructAS6_3(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS6_3 = new JSPlatGHAirSoil6_3(dev.getDeviceid(),"121345", as6_3);
                                    log.info("[debug]JS IOT 2023 report AirSoil6_3 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil6_3) jsPlatGHAS6_3).getData());
                                    boolean rspOK6 = httpCli.postMsg2JSPlat(jsPlatGHAS6_3);
                                    dev.incrMsgNum(rspOK6);
                                    dev.incrMsgPeriod(rspOK6);
                                    if (rspOK6) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil7":
                                    AirSoil7 as7 = jnmodel.constructAS7(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS7 = new JSPlatGHAirSoil7(dev.getDeviceid(),"121345", as7);
                                    log.info("[debug]JS IOT 2023 report AirSoil7 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil7) jsPlatGHAS7).getData());
                                    boolean rspOK7 = httpCli.postMsg2JSPlat(jsPlatGHAS7);
                                    dev.incrMsgNum(rspOK7);
                                    dev.incrMsgPeriod(rspOK7);
                                    if (rspOK7) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil7_2":
                                    AirSoil7_2 as7_2 = jnmodel.constructAS7_2(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS7_2 = new JSPlatGHAirSoil7_2(dev.getDeviceid(),"121345", as7_2);
                                    log.info("[debug]JS IOT 2023 report AirSoil7_2 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil7_2) jsPlatGHAS7_2).getData());
                                    boolean rspOK8 = httpCli.postMsg2JSPlat(jsPlatGHAS7_2);
                                    dev.incrMsgNum(rspOK8);
                                    dev.incrMsgPeriod(rspOK8);
                                    if (rspOK8) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghairsoil7_3":
                                    AirSoil7_3 as7_3 = jnmodel.constructAS7_3(dev.getStddist());
                                    JSPlatJNSession jsPlatGHAS7_3 = new JSPlatGHAirSoil7_3(dev.getDeviceid(),"121345", as7_3);
                                    log.info("[debug]JS IOT 2023 report AirSoil7_3 msg to JSIOTPlat： {}.", ((JSPlatGHAirSoil7_3) jsPlatGHAS7_3).getData());
                                    boolean rspOK7_3 = httpCli.postMsg2JSPlat(jsPlatGHAS7_3);
                                    dev.incrMsgNum(rspOK7_3);
                                    dev.incrMsgPeriod(rspOK7_3);
                                    if (rspOK7_3) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghsoil3":
                                    Soil3 s3 = jnmodel.constructSoil3(dev.getStddist());
                                    JSPlatJNSession jsPlatS3 = new JSPlatGHSoil3(dev.getDeviceid(),"121345", s3);
                                    log.info("[debug]JS IOT 2023 report Soil3 msg to JSIOTPlat： {}.", ((JSPlatGHSoil3) jsPlatS3).getData());
                                    boolean rspOK9 = httpCli.postMsg2JSPlat(jsPlatS3);
                                    dev.incrMsgNum(rspOK9);
                                    dev.incrMsgPeriod(rspOK9);
                                    if (rspOK9) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghsoil2":
                                    Soil2 s2 = jnmodel.constructSoil2(dev.getStddist());
                                    JSPlatJNSession jsPlatS2 = new JSPlatGHSoil2(dev.getDeviceid(),"121345", s2);
                                    log.info("[debug]JS IOT 2023 report Soil2 msg to JSIOTPlat： {}.", ((JSPlatGHSoil2) jsPlatS2).getData());
                                    boolean rspOK9_2 = httpCli.postMsg2JSPlat(jsPlatS2);
                                    dev.incrMsgNum(rspOK9_2);
                                    dev.incrMsgPeriod(rspOK9_2);
                                    if (rspOK9_2) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "ghair3":
                                    Air3 a3 = jnmodel.constructAir3(dev.getStddist());
                                    JSPlatJNSession jsPlatA3 = new JSPlatGHAir3(dev.getDeviceid(),"121345", a3);
                                    log.info("[debug]JS IOT 2023 report Air3 msg to JSIOTPlat： {}.", ((JSPlatGHAir3) jsPlatA3).getData());
                                    boolean rspOK10 = httpCli.postMsg2JSPlat(jsPlatA3);
                                    dev.incrMsgNum(rspOK10);
                                    dev.incrMsgPeriod(rspOK10);
                                    if (rspOK10) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "aquawa3":
                                    Water3 w3 = jnmodel.constructWater3(dev.getStddist());
                                    JSPlatJNSession jsPlatW3 = new JSPlatAquaWa3(dev.getDeviceid(),"121345", w3);
                                    log.info("[debug]JS IOT 2023 report Water3 msg to JSIOTPlat： {}.", ((JSPlatAquaWa3) jsPlatW3).getData());
                                    boolean rspOK11 = httpCli.postMsg2JSPlat(jsPlatW3);
                                    dev.incrMsgNum(rspOK11);
                                    dev.incrMsgPeriod(rspOK11);
                                    if (rspOK11) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "aquawa3_1":
                                    Water3_1 w31 = jnmodel.constructWater3_1(dev.getStddist());
                                    JSPlatJNSession jsPlatW31 = new JSPlatAquaWa3_1(dev.getDeviceid(),"121345",w31);
                                    log.info("[debug]JS IOT 2023 report Water3_1 msg to JSIOTPlat： {}.", ((JSPlatAquaWa3_1) jsPlatW31).getData());
                                    boolean rspOK12 = httpCli.postMsg2JSPlat(jsPlatW31);
                                    dev.incrMsgNum(rspOK12);
                                    dev.incrMsgPeriod(rspOK12);
                                    if (rspOK12) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "aquawa2":
                                    Water2 w2 = jnmodel.constructWater2(dev.getStddist());
                                    JSPlatJNSession jsPlatW2 = new JSPlatAquaWa2(dev.getDeviceid(),"121345", w2);
                                    log.info("[debug]JS IOT 2023 report Water2 msg to JSIOTPlat： {}.", ((JSPlatAquaWa2) jsPlatW2).getData());
                                    boolean rspOK13 = httpCli.postMsg2JSPlat(jsPlatW2);
                                    dev.incrMsgNum(rspOK13);
                                    dev.incrMsgPeriod(rspOK13);
                                    if (rspOK13) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "aquawa4":
                                    Water4 w4 = jnmodel.constructWater4(dev.getStddist());
                                    JSPlatJNSession jsPlatW4 = new JSPlatAquaWa4(dev.getDeviceid(),"121345", w4);
                                    log.info("[debug]JS IOT 2023 report Water4 msg to JSIOTPlat： {}.", ((JSPlatAquaWa4) jsPlatW4).getData());
                                    boolean rspOK14 = httpCli.postMsg2JSPlat(jsPlatW4);
                                    dev.incrMsgNum(rspOK14);
                                    dev.incrMsgPeriod(rspOK14);
                                    if (rspOK14) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                case "aquawa5":
                                    Water5 w5 = jnmodel.constructWater5(dev.getStddist());
                                    JSPlatJNSession jsPlatW5 = new JSPlatAquaWa5(dev.getDeviceid(),"121345",w5);
                                    log.info("[debug]JS IOT 2023 report Water5 msg to JSIOTPlat： {}.", ((JSPlatAquaWa5) jsPlatW5).getData());
                                    boolean rspOK12_2 = httpCli.postMsg2JSPlat(jsPlatW5);
                                    dev.incrMsgNum(rspOK12_2);
                                    dev.incrMsgPeriod(rspOK12_2);
                                    if (rspOK12_2) {
                                        log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
                                    } else {
                                        log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
                                    }
                                    break;

                                default:
                                    log.error("JS IOT 2023 period scan wrong groupname:{}.", model.getGroupname());
                                    break;
                            }
                            //更新上次数据上报时间
                            dev.setLstime(new Date());
                        }

                        //保存江宁主体的上报数据统计到数据库
                        if(saveDBfg){
                            log.info("[debug] JS IOT 2023 record device:{} into db.", dev.getDeviceid());
                            jnDevStatDAO.insert(dev);
                            dev.adjustMsgPeriod();
                            dev.resetNum();
                        }
                    }
                }
            }
        }

        log.info("JS IOT 2023 {}'st period scan end.", cycnt);

    }



    private void initMdlMatchFromFile(){
        StringBuilder jsonbuilder = readFile("json/def_jsiot_model.json");
        deviceDefLst = JacksonUtils.readValue(jsonbuilder.toString(), new TypeReference<List<JSIotDvcDef<JNAgriOrg>>>() { });
    }

    private void initDeviceMap(){
        for(JSIotDvcDef<JNAgriOrg> model:deviceDefLst){
            for(JNAgriOrg org : model.getOrglst()){
                for(String dvname : org.getDeviceidlst()){
                    log.info("JS IOT 2023 init {}:{} to DeviceMap.", org.getOrgname(), dvname);
                    JnDevicesMap.put(dvname, new JSPlatJNDevDO(dvname,org.getOrgname(), randTimeNext1Hour(), 16*60, (float)(Math.random()*0.05)));
                }
            }
        }
    }

    private void sendJSPlatMsg(JSPlatJNDevDO dev, JSPlatJNSession msg){
        boolean rspOK4 = httpCli.postMsg2JSPlat(msg);
        dev.incrMsgNum(rspOK4);
        dev.incrMsgPeriod(rspOK4);
        if (rspOK4) {
            log.info("JS IOT 2023 {}:{} report msg to JSIOTPlat succeed.", dev.getOwnername(), dev.getDeviceid());
        } else {
            log.error("JS IOT 2023 {}:{} report msg to JSIOTPlat fail!!!", dev.getOwnername(), dev.getDeviceid());
        }
    }

    /**
     * 生成并返回最近1小时后的随机时间
     */
    private Date randTimeNext1Hour(){
        long rndmsec = (long)(60*60*1000+Math.random()*5*60*1000);
        long utcnow = System.currentTimeMillis();
        return new Date(utcnow+rndmsec);
    }

    private StringBuilder readFile(String filename) {
        //读取到静态资源文件
        StringBuilder sb = new StringBuilder();
        try {
            //使用io读出数据
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename)));
            String str = null;
            while((str = br.readLine()) != null){
                sb.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }
}
