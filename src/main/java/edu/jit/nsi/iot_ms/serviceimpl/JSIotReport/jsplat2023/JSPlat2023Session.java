package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import lombok.Data;

@Data
public class JSPlat2023Session<T> {
    String deviceId;
    String sessionKey;
    T data;

    public JSPlat2023Session(String dvid, String sk, T d){
        deviceId =dvid;
        sessionKey = sk;
        data = d;
    }
}
