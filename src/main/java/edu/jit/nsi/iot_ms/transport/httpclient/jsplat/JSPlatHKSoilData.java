package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatHKSoilData extends JSPlatJNSession {
    SoilKV data;

    public JSPlatHKSoilData(String tid, String sk, float temp, float humi, float ec){
        super(tid,sk);
        data = new SoilKV(temp, humi, ec);
    }
    @Data
    @AllArgsConstructor
    private class SoilKV{
        float soilTemp;
        float soilMoisture;
        float soilEC;
    }
}
