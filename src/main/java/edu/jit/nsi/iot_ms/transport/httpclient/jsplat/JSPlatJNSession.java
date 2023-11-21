package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.Data;

@Data
public class JSPlatJNSession {
    String deviceId;
    String sessionKey;

    public JSPlatJNSession(String dvid, String sk){
        deviceId =dvid;
        sessionKey = sk;
    }
}
