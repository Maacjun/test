package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Soil3{
    float soilTemp;      //土壤温度
    float soilMoisture;  //土壤湿度
    float electroconductibility; //电导率 ms/cm  ec/1000

    public void reSetElectroconductibility() {
        electroconductibility = electroconductibility/1000;
    }
}
