package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport;


import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023.*;
import edu.jit.nsi.iot_ms.transport.ReportData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class JSIotJNDataModel {
    @Autowired
    JSIotRoutine iotRoutine;

    //汇聚园艺站数据到这个对象中的记录
    Air3 air3;
    Air4 air4;
    AirSoil4_1 as4_1;
    AirSoil4_2 as4_2;
    AirSoil4_3 as4_3;
    AirSoil5 as5;
    AirSoil5 as5_2;
    AirSoil6_1 as6_1;
    AirSoil6_2 as6_2;
    AirSoil6_3 as6_3;
    AirSoil7 as7;
    AirSoil7_2 as7_2;
    Soil3 s3;
    AirSoil7_3 as7_3;
    Soil2 s2;

    Water3 water3;
    Water3_1 water3_1;
    Water2 water2;
    Water4 water4;
    Water5 water5;

    //不同环境参数的波动范围
    ParamFluctRange tempFR;
    ParamFluctRange humiFR;
    ParamFluctRange illuFR;
    ParamFluctRange co2FR;
    ParamFluctRange soilTempFR;
    ParamFluctRange soilHumiFR;
    ParamFluctRange soilECFR;
    ParamFluctRange doFR;
    ParamFluctRange phFR;
    ParamFluctRange waTempFR;
    ParamFluctRange waECFR;
    ParamFluctRange waSSFR;


    public JSIotJNDataModel(){
        air3 = new Air3();  //temp:  illu:
        air4= new Air4();
        as4_1 = new AirSoil4_1();
        as4_2 = new AirSoil4_2();
        as4_3 = new AirSoil4_3();
        as5 = new AirSoil5();
        as5_2 = new AirSoil5();
        as6_1 = new AirSoil6_1();
        as6_2 = new AirSoil6_2();
        as6_3 = new AirSoil6_3();
        as7 = new AirSoil7();
        as7_2 = new AirSoil7_2();
        as7_3 = new AirSoil7_3();
        s3 = new Soil3();
        s2 = new Soil2();

        water3 = new Water3();
        water3_1 = new Water3_1();
        water2 = new Water2();
        water4 = new Water4();
        water5 = new Water5();

        tempFR = new ParamFluctRange((float)-10.0,(float)35.0,(float) 0.02);
        humiFR = new ParamFluctRange((float) 0.0,(float)100.0,(float) 0.02);
        illuFR = new ParamFluctRange((float) 0.0,(float)50.0,(float) 0.02); //只有白天生效
        co2FR= new ParamFluctRange((float) 300.0,(float)500.0,(float) 0.01);
        soilTempFR = new ParamFluctRange((float) 0.0,(float)20.0,(float) 0.01);
        soilHumiFR = new ParamFluctRange((float) 20.0,(float)90.0,(float) 0.01);
        soilECFR= new ParamFluctRange((float)100,(float)700.0,(float) 0.01);
        doFR = new ParamFluctRange((float) 1.0,(float)8.0,(float) 0.02);
        phFR = new ParamFluctRange((float)6.0,(float)8.0,(float) 0.01);
        waTempFR = new ParamFluctRange((float)10.0,(float)30.0,(float) 0.01);
        waECFR = new ParamFluctRange((float)100.0,(float)800.0,(float) 0.01);
        waSSFR = new ParamFluctRange((float)0.0,(float)30.0,(float) 0.01);
    }

    public void updtModel(int termid, ReportData rtpdata){
        int cntr = 0;
        //获取更新的模板
        List<String> modlelst = iotRoutine.getMachemdl(termid);
//        List<String> modlelst = new ArrayList<>();
//        modlelst.add("ghair4");
//        modlelst.add("ghairsoil4_1");
//        modlelst.add("ghairsoil6_3");
//        modlelst.add("ghairsoil6_2");
//        modlelst.add("ghairsoil7");

        if(modlelst.size()==0)
            return;
        log.info("JS IOT 2023 update model data start,  term:{}, type:{}, model list size:{}.", termid, rtpdata.getType(), modlelst.size());
        for(String modname:modlelst){
            switch (modname){
                case "ghair4":
                    if(rtpdata.getType().equals("temp"))
                        air4.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        air4.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        air4.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("co2"))
                        air4.setDioxideCond(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil4_1":
                    if(rtpdata.getType().equals("temp"))
                        as4_1.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as4_1.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as4_1.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as4_1.setSoilMoisture(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil4_2":
                    if(rtpdata.getType().equals("temp"))
                        as4_2.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as4_2.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as4_2.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as4_2.setSoilTemp(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil4_3":
                    if(rtpdata.getType().equals("temp"))
                        as4_3.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as4_3.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        as4_3.setWaterPH(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ec_u"))
                        as4_3.setElectroconductibility(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil5":
                    if(rtpdata.getType().equals("temp"))
                        as5.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as5.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as5.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as5.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as5.setSoilMoisture(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil5_2":
                    if(rtpdata.getType().equals("temp"))
                        as5_2.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as5_2.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as5_2.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as5_2.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as5_2.setSoilMoisture(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil6_1":
                    if(rtpdata.getType().equals("temp"))
                        as6_1.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as6_1.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as6_1.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as6_1.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as6_1.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("co2"))
                        as6_1.setDioxideCond(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil6_2":
                    if(rtpdata.getType().equals("temp"))
                        as6_2.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as6_2.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as6_2.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as6_2.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as6_2.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilEC"))
                        as6_2.setConductivity(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil6_3":
                    if(rtpdata.getType().equals("temp"))
                        as6_3.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as6_3.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as6_3.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as6_3.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilEC"))
                        as6_3.setElectroconductibility(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as6_3.setLightIntensityTwo(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil7":
                    if(rtpdata.getType().equals("temp"))
                        as7.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as7.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as7.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as7.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as7.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("co2"))
                        as7.setDioxideCond(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilEC"))
                        as7.setSoilsalt(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil7_2":
                    if(rtpdata.getType().equals("temp"))
                        as7_2.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        as7_2.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as7_2.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as7_2.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("co2"))
                        as7_2.setDioxideCond(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilEC"))
                        as7_2.setElectroconductibility(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as7_2.setLightIntensityTwo(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghairsoil7_3":
                    if(rtpdata.getType().equals("temp")) {
                        as7_3.setAirTemp(rtpdata.getValue());
                        as7_3.setDewPoint(rtpdata.getValue());
                    }
                    else if(rtpdata.getType().equals("humi"))
                        as7_3.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        as7_3.setLightIntensity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilTemp"))
                        as7_3.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        as7_3.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("co2"))
                        as7_3.setDioxideCond(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghsoil3":
                    if(rtpdata.getType().equals("soilTemp"))
                        s3.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        s3.setSoilMoisture(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilEC"))
                        s3.setElectroconductibility(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghsoil2":
                    if(rtpdata.getType().equals("soilTemp"))
                        s2.setSoilTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("soilHumi"))
                        s2.setSoilMoisture(rtpdata.getValue());
                    cntr++;
                    break;

                case "ghair3":
                    if(rtpdata.getType().equals("temp"))
                        air3.setAirTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("humi"))
                        air3.setAirHumidity(rtpdata.getValue());
                    else if(rtpdata.getType().equals("illu"))
                        air3.setLightIntensity(rtpdata.getValue());
                    cntr++;
                    break;

                case "aquawa3":
                    if(rtpdata.getType().equals("do"))
                        water3.setWaterOxygen(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        water3.setPh(rtpdata.getValue());
                    else if(rtpdata.getType().equals("waTemp"))
                        water3.setWaterTemp(rtpdata.getValue());
                    cntr++;
                    break;

                case "aquawa3_1":
                    if(rtpdata.getType().equals("do"))
                        water3_1.setDissolveO(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        water3_1.setPh(rtpdata.getValue());
                    else if(rtpdata.getType().equals("waTemp"))
                        water3_1.setWaterTemp(rtpdata.getValue());
                    cntr++;
                    break;

                case "aquawa2":
                    if(rtpdata.getType().equals("do"))
                        water2.setDissolveO(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        water2.setPh(rtpdata.getValue());
                    cntr++;
                    break;

                case "aquawa4":
                    if(rtpdata.getType().equals("do"))
                        water4.setDissolveO(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        water4.setPh(rtpdata.getValue());
                    else if(rtpdata.getType().equals("waTemp"))
                        water4.setWaterTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ec_u"))
                        water4.setConductivity(rtpdata.getValue());
                    cntr++;
                    break;

                case "aquawa5":
                    if(rtpdata.getType().equals("do"))
                        water5.setDissolveO(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ph"))
                        water5.setPh(rtpdata.getValue());
                    else if(rtpdata.getType().equals("waTemp"))
                        water5.setWaterTemp(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ec_u"))
                        water5.setElectroconductibility(rtpdata.getValue());
                    else if(rtpdata.getType().equals("ss"))
                        water5.setTurbidity(rtpdata.getValue());
                    cntr++;
                    break;

                default:
                    log.error("JS IOT 2023 update model date wrong groupname:{}.", modname);
                    break;
            }
        }

        log.info("JS IOT 2023 update model data end,  update counter:{}.", cntr);
    }

    public Air3 constructAir3(float ofrate){
        float tmp = calcTemp(air3.getAirTemp(),ofrate);
        float hu = calcHumi(air3.getAirHumidity(),ofrate);
        float il = calcIllu(air3.getLightIntensity(),ofrate);
        return new Air3(tmp, hu, il);
    }


    public Air4 constructAir4(float ofrate){
        float tmp = calcTemp(air4.getAirTemp(),ofrate);
        float hu = calcHumi(air4.getAirHumidity(),ofrate);
        float il = calcIllu(air4.getLightIntensity(),ofrate);
        float c = calcCo2(air4.getDioxideCond(), ofrate);
        return new Air4(tmp, hu, il, c);
    }

    public AirSoil4_1 constructAS4_1(float ofrate){
        float tmp = calcTemp(as4_1.getAirTemp(),ofrate);
        float hu = calcHumi(as4_1.getAirHumidity(),ofrate);
        float il = calcIllu(as4_1.getLightIntensity(),ofrate);
        float sh = calcSoilHumi(as4_1.getSoilMoisture(), ofrate);
        return new AirSoil4_1(tmp, hu, sh, il);
    }

    public AirSoil4_2 constructAS4_2(float ofrate){
        float tmp = calcTemp(as4_2.getAirTemp(),ofrate);
        float hu = calcHumi(as4_2.getAirHumidity(),ofrate);
        float il = calcIllu(as4_2.getLightIntensity(),ofrate);
        float st = calcSoilTemp(as4_2.getSoilTemp(), ofrate);
        return new AirSoil4_2(tmp, hu, st, il);
    }

    public AirSoil4_3 constructAS4_3(float ofrate){
        float tmp = calcTemp(as4_3.getAirTemp(),ofrate);
        float hu = calcHumi(as4_3.getAirHumidity(),ofrate);
        float ph = calcIllu(as4_3.getWaterPH(),ofrate);
        float ec = calcSoilTemp(as4_3.getElectroconductibility(), ofrate);
        AirSoil4_3 as43 = new AirSoil4_3(tmp, hu, ph, ec);
        as43.reSetElectroconductibility();
        return as43;
    }

    public AirSoil5 constructAS5(float ofrate){
        float tmp = calcTemp(as5.getAirTemp(),ofrate);
        float hu = calcHumi(as5.getAirHumidity(),ofrate);
        float il = calcIllu(as5.getLightIntensity(),ofrate);
        float st = calcSoilTemp(as5.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as5.getSoilMoisture(), ofrate);
        return new AirSoil5(tmp, hu, st, sh, il);
    }

    public AirSoil6_1 constructAS6_1(float ofrate){
        float tmp = calcTemp(as6_1.getAirTemp(),ofrate);
        float hu = calcHumi(as6_1.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as6_1.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as6_1.getSoilMoisture(), ofrate);
        float il = calcIllu(as6_1.getLightIntensity(),ofrate);
        float c = calcCo2(as6_1.getDioxideCond(), ofrate);
        return new AirSoil6_1(tmp, hu, st, sh, il, c);
    }

    public AirSoil6_2 constructAS6_2(float ofrate){
        float tmp = calcTemp(as6_2.getAirTemp(),ofrate);
        float hu = calcHumi(as6_2.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as6_2.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as6_2.getSoilMoisture(), ofrate);
        float il = calcIllu(as6_2.getLightIntensity(),ofrate);
        float ec = calcSoilEC(as6_2.getConductivity(), ofrate);
        AirSoil6_2 soil6_2 = new AirSoil6_2(tmp, hu, st, sh, il, ec);
        soil6_2.reSetConductivity();
        return soil6_2;
    }

    public AirSoil6_3 constructAS6_3(float ofrate){
        float tmp = calcTemp(as6_3.getAirTemp(),ofrate);
        float hu = calcHumi(as6_3.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as6_3.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as6_3.getSoilMoisture(), ofrate);
        float il = calcIllu(as6_3.getLightIntensityTwo(),ofrate);
        float ec = calcSoilEC(as6_3.getElectroconductibility(), ofrate);
        AirSoil6_3  soil6_3 = new AirSoil6_3(tmp, hu, st, sh, ec, il);
        soil6_3.reSetElectroconductibility();
        soil6_3.reSetLightIntensityTwo();
        return soil6_3;
    }

    public AirSoil7 constructAS7(float ofrate){
        float tmp = calcTemp(as7.getAirTemp(),ofrate);
        float hu = calcHumi(as7.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as7.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as7.getSoilMoisture(), ofrate);
        float il = calcIllu(as7.getLightIntensity(),ofrate);
        float ec = calcSoilEC(as7.getSoilsalt(), ofrate);
        float co = calcCo2(as7.getDioxideCond(), ofrate);
        AirSoil7 soil7 = new AirSoil7(tmp, hu, st, sh, il, ec, co);
        soil7.reSetsoilsalt();
        return soil7;
    }

    public AirSoil7_2 constructAS7_2(float ofrate){
        float tmp = calcTemp(as7_2.getAirTemp(),ofrate);
        float hu = calcHumi(as7_2.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as7_2.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as7_2.getSoilMoisture(), ofrate);
        float co = calcCo2(as7_2.getDioxideCond(), ofrate);
        float ec = calcSoilEC(as7_2.getElectroconductibility(), ofrate);
        float il = calcIllu(as7_2.getLightIntensityTwo(),ofrate);
        AirSoil7_2  soil7_2 = new AirSoil7_2(tmp, hu, st, sh, co, ec, il);
        soil7_2.reSetElectroconductibility();
        soil7_2.reSetLightIntensityTwo();
        return soil7_2;
    }

    public AirSoil7_3 constructAS7_3(float ofrate){
        float tmp = calcTemp(as7_3.getAirTemp(),ofrate);
        float hu = calcHumi(as7_3.getAirHumidity(),ofrate);
        float st = calcSoilTemp(as7_3.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(as7_3.getSoilMoisture(), ofrate);
        float il = calcIllu(as7_3.getLightIntensity(),ofrate);
        float co = calcCo2(as7_3.getDioxideCond(), ofrate);
        AirSoil7_3 soil7_3 = new AirSoil7_3(tmp, hu, st, sh, il, co, tmp);
        return soil7_3;
    }

    public Soil3 constructSoil3(float ofrate){
        float st = calcSoilTemp(s3.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(s3.getSoilMoisture(), ofrate);
        float ec = calcSoilEC(s3.getElectroconductibility(), ofrate);
        Soil3 sl3 = new Soil3(st, sh, ec);
        sl3.reSetElectroconductibility();
        return sl3;
    }

    public Soil2 constructSoil2(float ofrate){
        float st = calcSoilTemp(s3.getSoilTemp(), ofrate);
        float sh = calcSoilHumi(s3.getSoilMoisture(), ofrate);
        Soil2 sl2 = new Soil2(st, sh);
        return sl2;
    }

    public Water3 constructWater3(float ofrate){
        float d = calcDo(water3.getWaterOxygen(), ofrate);
        float p = calcPh(water3.getPh(), ofrate);
        float t = calcWaTemp(water3.getWaterTemp(), ofrate);
        return new Water3(d,p,t);
    }

    public Water3_1 constructWater3_1(float ofrate){
        float d = calcDo(water3_1.getDissolveO(), ofrate);
        float p = calcPh(water3_1.getPh(), ofrate);
        float t = calcWaTemp(water3_1.getWaterTemp(), ofrate);
        log.info("JS IOT 2023 constructWater3_1 do:{}, ph:{}, waTemp:{}.", d,p,t);
        return new Water3_1(d,p,t);
    }

    public Water4 constructWater4(float ofrate){
        float d = calcDo(water4.getDissolveO(), ofrate);
        float p = calcPh(water4.getPh(), ofrate);
        float t = calcWaTemp(water4.getWaterTemp(), ofrate);
        float e = calcWaEC(water4.getConductivity(), ofrate);
        Water4 w4 = new Water4(d,p,t,e);
        w4.reSetConductivity();
        return w4;
    }

    public Water2 constructWater2(float ofrate){
        float d = calcDo(water2.getDissolveO(), ofrate);
        float p = calcPh(water2.getPh(), ofrate);
        return new Water2(d,p);
    }

    public Water5 constructWater5(float ofrate){
        float d = calcDo(water5.getDissolveO(), ofrate);
        float p = calcPh(water5.getPh(), ofrate);
        float t = calcWaTemp(water5.getWaterTemp(), ofrate);
        float e = calcWaEC(water5.getElectroconductibility(), ofrate);
        float s =  calcWaSS(water5.getTurbidity(), ofrate);
        log.info("JS IOT 2023 constructWater5 do:{}, ph:{}, waTemp:{}, ec:{}, ss:{}.", d,p,t,e,s);
        return new Water5(d,p,t,e,s);
    }

    private float calcTemp(float mdlvalue, float offsetrate){
        float fd =  mdlvalue + (tempFR.getUpper()-tempFR.getLower())*(offsetrate+tempFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcHumi(float mdlvalue, float offsetrate){
        float fd = mdlvalue+(humiFR.getUpper()-humiFR.getLower())*(offsetrate+humiFR.getFluctrate()*(float) Math.random());
        if(fd>100.0){
            fd=100.0f;
        }
        return floatFormat(fd);
    }

    private float calcIllu(float mdlvalue, float offsetrate){
        if(mdlvalue<5.0) {
            return 0.0f;
        }
        else{
            float fd = mdlvalue+(illuFR.getUpper()-illuFR.getLower())*(offsetrate+illuFR.getFluctrate()*(float) Math.random());
            fd=(float) (fd*1.0/1000.0);
            return floatFormat(fd);
        }
    }

    private float calcCo2(float mdlvalue, float offsetrate){
        float fd =  350+ mdlvalue+(co2FR.getUpper()-co2FR.getLower())*(offsetrate+co2FR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcSoilTemp(float mdlvalue, float offsetrate){
        float fd =   mdlvalue+(soilTempFR.getUpper()-soilTempFR.getLower())*(offsetrate+soilTempFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcSoilHumi(float mdlvalue, float offsetrate){
        float fd =   mdlvalue+(soilHumiFR.getUpper()-soilHumiFR.getLower())*(offsetrate+soilHumiFR.getFluctrate()*(float) Math.random());
        if(fd>100.0){
            fd=100.0f;
        }
        return floatFormat(fd);
    }

    private float calcSoilEC(float mdlvalue, float offsetrate){
        float fd =   mdlvalue+(soilECFR.getUpper()-soilECFR.getLower())*(offsetrate+soilECFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcDo(float mdlvalue, float offsetrate){
        float fd =   mdlvalue+(doFR.getUpper()-doFR.getLower())*(offsetrate+doFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcPh(float mdlvalue, float offsetrate){
        float fd =   mdlvalue+(phFR.getUpper()-phFR.getLower())*(offsetrate+phFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcWaTemp(float mdlvalue, float offsetrate){
        log.info("JS IOT 2023 model value:{},  offsetrate:{}, fluctrate:{}", mdlvalue, offsetrate, waTempFR.getFluctrate());
        float fd =   mdlvalue+(waTempFR.getUpper()-waTempFR.getLower())*(offsetrate+waTempFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcWaEC(float mdlvalue, float offsetrate){
        float fd = mdlvalue+(waECFR.getUpper()-waECFR.getLower())*(offsetrate+waECFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float calcWaSS(float mdlvalue, float offsetrate){
        float fd = mdlvalue+(waSSFR.getUpper()-waSSFR.getLower())*(offsetrate+waSSFR.getFluctrate()*(float) Math.random());
        return floatFormat(fd);
    }

    private float floatFormat(float tmp){
        return (float) ((float)Math.round(tmp*10)*1.0/10);
    }
    @Data
    @AllArgsConstructor
    class ParamFluctRange{
        float lower;   //有效数据下界
        float upper;   //有效数据上界
        float fluctrate; //随机波动范围，5%
    }
}
