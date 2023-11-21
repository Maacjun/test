package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirSoil7{
    float airTemp;       //温度
    float airHumidity;   //湿度
    float soilTemp;      //土壤温度
    float soilMoisture;  //土壤湿度
    float lightIntensity;//光照 klux
    float dioxideCond;   //co2
    float soilsalt;      //土壤盐度

    public void reSetsoilsalt(){
        float tmp = (float) ((soilsalt+41.2653)/2120.76*1000);
        soilsalt = (float) ((float)Math.round(tmp*100)*1.0/100);//保留小数点后两位
    }
}
