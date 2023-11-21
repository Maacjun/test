package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirSoil7_2{
    float airTemp;       //温度
    float airHumidity;   //湿度
    float soilTemp;      //土壤温度
    float soilMoisture;  //土壤湿度
    float dioxideCond;   //co2
    float electroconductibility; //电导率 ms/cm  ec/1000
    float lightIntensityTwo;//光照 lux*1000

    public void reSetElectroconductibility() {
        float tmp = electroconductibility/1000;
        electroconductibility = (float) ((float)Math.round(tmp*1000)*1.0/1000);//保留小数点后三位
    }

    public void reSetLightIntensityTwo() {
        this.lightIntensityTwo = lightIntensityTwo*1000;
    }
}
