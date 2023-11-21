package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatJCSoilData extends JSPlatJNSession {
    SoilKV data;

    public JSPlatJCSoilData(String tid, String sk, float temp, float humi, float tds, float ec){
        super(tid,sk);
        data = new SoilKV(temp, humi, tds, ec);
    }
    @Data
    @AllArgsConstructor
    private class SoilKV{
        float soilTemp;
        float soilMoisture;
        float soilsalt;
        float soilEC;
    }
}
