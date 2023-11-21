package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatJCAirTHData extends JSPlatJNSession {
    AirTHKV data;

    public JSPlatJCAirTHData(String tid, String sk, float temp, float humi){
        super(tid,sk);
        data = new AirTHKV(temp, humi);
    }
    @Data
    @AllArgsConstructor
    private class AirTHKV{
        float airTemp;
        float airHumidity;
    }
}
