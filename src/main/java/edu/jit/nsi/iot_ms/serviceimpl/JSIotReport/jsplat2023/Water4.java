package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Water4 {
    float dissolveO;  //溶解氧值
    float ph;           //ph
    float waterTemp;    //水体温度
    float conductivity;  //电导率

    public void reSetConductivity() {
        this.conductivity = conductivity/100;
    }
}