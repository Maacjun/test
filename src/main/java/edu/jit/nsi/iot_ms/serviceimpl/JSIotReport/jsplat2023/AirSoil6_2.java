package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirSoil6_2{
    float airTemp;       //温度
    float airHumidity;   //湿度
    float soilTemp;      //土壤温度
    float soilMoisture;  //土壤湿度
    float lightIntensity;//光照 klux
    float conductivity;  //单位s/m 电导率=ec/10000

    public void reSetConductivity() {
        float tmp = conductivity/10000;
        conductivity = (float) ((float)Math.round(tmp*1000)*1.0/1000);//保留小数点后三位
    }

}
