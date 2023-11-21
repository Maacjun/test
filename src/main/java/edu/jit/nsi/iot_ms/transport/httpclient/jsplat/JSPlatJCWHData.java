package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.Random;

@Setter
@Data
public class JSPlatJCWHData extends JSPlatJNSession {
    @JsonIgnore
    private final RangePrecise tempfactor = new RangePrecise(2,(float)0.1);
    @JsonIgnore
    private final RangePrecise humifactor =  new RangePrecise(5, (float)0.1);   //不超过100，不低于0
    @JsonIgnore
    private final RangePrecise illuactor =  new RangePrecise(100,1);   //不低于0
    @JsonIgnore
    private final RangePrecise dirfactor =  new RangePrecise(20,1);     //不超过360，不低于0
    @JsonIgnore
    private final RangePrecise spdfactor =  new RangePrecise(2, (float)0.1);      //不低于0
    @JsonIgnore
    private final RangePrecise stempfactor =  new RangePrecise(1, (float)0.1);
    @JsonIgnore
    private final RangePrecise shumifactor =  new RangePrecise(40, (float)0.1);  //不超过100，不低于0
    @JsonIgnore
    private final RangePrecise ecfactor =  new RangePrecise(10,(float)0.1);  //不低于0
    @JsonIgnore
    DecimalFormat df = new DecimalFormat("0.0");

    JCKV data;

    @JsonIgnore
    Random r = new Random();

    public JSPlatJCWHData(){
        super("", "");
        data = new JCKV(0,0,0,0,0,0,0,0,0,0);
    }

    public JSPlatJCWHData(String tid, String sk, float temp, float humi, float ap, float illu,
                          float dir, float spd,  float rain,float stemp, float shumi, float sec){
        super(tid,sk);
        data = new JCKV(temp, humi, ap, illu, dir, spd, rain, stemp, shumi, sec);
    }

    public JSPlatJCWHData(String newid, JSPlatJCWHData another){
        super(newid, another.sessionKey);
        data = new JCKV(0,0,0,0,0,0,0,0,0,0);
        //温度变换
        if(r.nextBoolean()){
            data.airTemp = another.data.airTemp + r.nextInt((int)(tempfactor.range/tempfactor.precise)) / (1/tempfactor.precise);
        }else{
            data.airTemp = another.data.airTemp - r.nextInt((int)(tempfactor.range/tempfactor.precise)) / (1/tempfactor.precise);
        }

        //湿度变换
        if(r.nextBoolean()){
            data.airHumidity = another.data.airHumidity+  r.nextInt((int)(humifactor.range/humifactor.precise)) / (1/humifactor.precise);
        }else{
            data.airHumidity = another.data.airHumidity -  r.nextInt((int)(humifactor.range/humifactor.precise)) / (1/humifactor.precise);
        }

        //气压
        data.atmos=another.data.atmos;

        //光照变换
        if(r.nextBoolean()){
            data.lightIntensity = another.data.lightIntensity + r.nextInt((int)(illuactor.range/illuactor.precise)) / (1/illuactor.precise);
        }else{
            data.lightIntensity = another.data.lightIntensity - r.nextInt((int)(illuactor.range/illuactor.precise)) / (1/illuactor.precise);
        }

        //风向变换
        if(r.nextBoolean()){
            data.windDirection = another.data.windDirection+ r.nextInt((int)(dirfactor.range/dirfactor.precise)) / (1/dirfactor.precise);
        }else{
            data.windDirection = another.data.windDirection- r.nextInt((int)(dirfactor.range/dirfactor.precise)) / (1/dirfactor.precise);
        }

        //风速变换
        if(r.nextBoolean()){
            data.windVelocity =another.data.windVelocity+ r.nextInt((int)(spdfactor.range/spdfactor.precise)) / (1/spdfactor.precise);
        }else{
            data.windVelocity =another.data.windVelocity- r.nextInt((int)(spdfactor.range/spdfactor.precise)) / (1/spdfactor.precise);
        }

        //土壤温度变换
        if(r.nextBoolean()){
            data.soilTemp =another.data.soilTemp+ r.nextInt((int)(stempfactor.range/stempfactor.precise)) / (1/stempfactor.precise);
        }else{
            data.soilTemp =another.data.soilTemp- r.nextInt((int)(stempfactor.range/stempfactor.precise)) / (1/stempfactor.precise);
        }

        //土壤湿度变换
        if(r.nextBoolean()){
            data.soilMoisture =another.data.soilMoisture+ r.nextInt((int)(shumifactor.range/shumifactor.precise)) / (1/shumifactor.precise);
        }else{
            data.soilMoisture =another.data.soilMoisture- r.nextInt((int)(shumifactor.range/shumifactor.precise)) / (1/shumifactor.precise);
        }

        //EC变换
        if(r.nextBoolean()){
            data.soilEC =another.data.soilEC+ r.nextInt((int)(ecfactor.range/ecfactor.precise)) / (1/ecfactor.precise);
        }else{
            data.soilEC =another.data.soilEC- r.nextInt((int)(ecfactor.range/ecfactor.precise)) / (1/ecfactor.precise);
        }

        checkIllegal();
    }

    @Data
    @AllArgsConstructor
    private class JCKV{
        /*空气温度(airTemp)、空气湿度(airHumidity)、大气压力(atmos)、光照(lightIntensity)、
        风向(windDirection)、风速(windVelocity)、降雨量(rainfall)、
        土壤温度(soilTemp)、土壤湿度(soilMoisture)、土壤EC值(soilEC)*/

        float airTemp;       //温度
        float airHumidity;   //湿度
        float atmos;         //气压
        float lightIntensity;//光照 klux
        float windDirection; //风向
        float windVelocity;  //风速
        float dayRainfall;   //24小时降雨量
        float soilTemp;      //土温
        float soilMoisture;  //土湿
        float soilEC;        //EC值
    }

    public void setDidKey(String did, String key){
        deviceId=did;
        sessionKey=key;
    }

    public void setJCKVData(float te, float hu, float at, float li, float wd, float wv, float rain, float st, float sm, float ec){
        data.setAirTemp(te);
        data.setAirHumidity(hu);
        data.setAtmos(at);
        data.setLightIntensity(li);
        data.setWindDirection(wd);
        data.setWindVelocity(wv);
        data.setDayRainfall(rain);
        data.setSoilTemp(st);
        data.setSoilMoisture(sm);
        data.setSoilEC(ec);
    }

    @Data
    @AllArgsConstructor
    private class RangePrecise{
        int range;      //变化范围
        float precise;  //精度
    }

    public void randData(){
        //温度变换
        if(r.nextBoolean()){
            data.airTemp += r.nextInt((int)(tempfactor.range/tempfactor.precise)) / (1/tempfactor.precise);
        }else{
            data.airTemp -= r.nextInt((int)(tempfactor.range/tempfactor.precise)) / (1/tempfactor.precise);
        }

        //湿度变换
        if(r.nextBoolean()){
            data.airHumidity += r.nextInt((int)(humifactor.range/humifactor.precise)) / (1/humifactor.precise);
        }else{
            data.airHumidity -= r.nextInt((int)(humifactor.range/humifactor.precise)) / (1/humifactor.precise);
        }

        //光照变换
        if(data.lightIntensity!=0){
            if(r.nextBoolean()){
                data.lightIntensity += r.nextInt((int)(illuactor.range/illuactor.precise)) / (1/illuactor.precise);
            }else{
                data.lightIntensity -= r.nextInt((int)(illuactor.range/illuactor.precise)) / (1/illuactor.precise);
            }
        }

        //风向变换
        if(r.nextBoolean()){
            data.windDirection += r.nextInt((int)(dirfactor.range/dirfactor.precise)) / (1/dirfactor.precise);
        }else{
            data.windDirection -= r.nextInt((int)(dirfactor.range/dirfactor.precise)) / (1/dirfactor.precise);
        }

        //风速变换
        if(data.windVelocity!=0){
            if(r.nextBoolean()){
                data.windVelocity += r.nextInt((int)(spdfactor.range/spdfactor.precise)) / (1/spdfactor.precise);
            }else{
                data.windVelocity -= r.nextInt((int)(spdfactor.range/spdfactor.precise)) / (1/spdfactor.precise);
            }
        }


        //土壤温度变换
        if(r.nextBoolean()){
            data.soilTemp += r.nextInt((int)(stempfactor.range/stempfactor.precise)) / (1/stempfactor.precise);
        }else{
            data.soilTemp -= r.nextInt((int)(stempfactor.range/stempfactor.precise)) / (1/stempfactor.precise);
        }

        //土壤湿度变换
        if(r.nextBoolean()){
            data.soilMoisture += r.nextInt((int)(shumifactor.range/shumifactor.precise)) / (1/shumifactor.precise);
        }else{
            data.soilMoisture -= r.nextInt((int)(shumifactor.range/shumifactor.precise)) / (1/shumifactor.precise);
        }

        //EC变换
        if(data.soilEC!=0){
            if(r.nextBoolean()){
                data.soilEC += r.nextInt((int)(ecfactor.range/ecfactor.precise)) / (1/ecfactor.precise);
            }else{
                data.soilEC -= r.nextInt((int)(ecfactor.range/ecfactor.precise)) / (1/ecfactor.precise);
            }
        }


        checkIllegal();
    }


    private void checkIllegal(){
        //湿度
        if(data.airHumidity<0){
            data.airHumidity=0;
        }
        if(data.airHumidity>100){
            data.airHumidity=100;
        }
        //光照
        if(data.lightIntensity<0){
            data.lightIntensity=0;
        }
        //风向
        if(data.windDirection<0){
            data.windDirection=0;
        }
        if(data.windDirection>360){
            data.windDirection=360;
        }
        //风速
        if(data.windVelocity<0){
            data.windVelocity=0;
        }
        //降雨量
        if(data.dayRainfall<0){
            data.dayRainfall=0;
        }
        //土壤湿度
        if(data.soilMoisture<0){
            data.soilMoisture=0;
        }
        if(data.soilMoisture>100){
            data.soilMoisture=100;
        }
        //ec
        if(data.soilEC<0){
            data.soilEC=0;
        }
        data.airTemp = Float.parseFloat(df.format(data.airTemp));
        data.airHumidity = Float.parseFloat(df.format(data.airHumidity));
//        data.atmos = Float.parseFloat(df.format(data.atmos));
        data.lightIntensity = Float.parseFloat(df.format(data.lightIntensity));
        data.windDirection = Float.parseFloat(df.format(data.windDirection));
        data.windVelocity = Float.parseFloat(df.format(data.windVelocity));
        data.dayRainfall = Float.parseFloat(df.format(data.dayRainfall));
        data.soilTemp = Float.parseFloat(df.format(data.soilTemp));
        data.soilMoisture = Float.parseFloat(df.format(data.soilMoisture));
        data.soilEC = Float.parseFloat(df.format(data.soilEC));
    }

    @Override
    public String toString() {
        return "deveui:"+deviceId+",  airTemp:"+data.airTemp+", airHumidity:"+data.airHumidity+", atmos:"+data.atmos+", lightIntensity:"+data.lightIntensity
                +", windDirection:"+data.windDirection+", windVelocity:"+data.windVelocity+", dayRainfall:"+data.dayRainfall+
                ", soilTemp:"+data.soilTemp+", soilMoisture:"+data.soilMoisture+", soilEC:"+data.soilEC;

    }
}
