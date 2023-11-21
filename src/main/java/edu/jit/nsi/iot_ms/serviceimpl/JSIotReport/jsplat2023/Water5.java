package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Water5 {
    float dissolveO;  //水体含氧量
    float ph;           //ph
    float waterTemp;    //水体温度
    float electroconductibility;  //电导率2
    float turbidity;    //浊度
}