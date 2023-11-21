package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirSoil7_3 {
    float airTemp;       //温度
    float airHumidity;   //湿度
    float soilTemp;      //土壤温度
    float soilMoisture;  //土壤湿度
    float lightIntensity;//光照 klux
    float dioxideCond;   //co2
    float DewPoint;      //露点温度
}
