package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Water3 {
    float waterOxygen;  //水体含氧量
    float ph;           //ph
    float waterTemp;    //水体温度
}