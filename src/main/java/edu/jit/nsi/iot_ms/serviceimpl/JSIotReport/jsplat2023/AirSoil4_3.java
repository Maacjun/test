package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirSoil4_3 {
    float airTemp;       //温度
    float airHumidity;   //湿度
    float waterPH;      //水体酸碱度
    float electroconductibility; //电导率 ms/cm

    public void reSetElectroconductibility() {
        electroconductibility = electroconductibility/1000;
    }
}