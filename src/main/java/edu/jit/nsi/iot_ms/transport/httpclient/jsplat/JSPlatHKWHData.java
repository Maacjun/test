package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatHKWHData extends JSPlatJNSession {
    WHKV data;

    public JSPlatHKWHData(String tid, String sk, float temp, float humi, float illu, float spd, float dir, float rain){
        super(tid,sk);
        data = new WHKV(temp, humi, illu, spd, dir, rain);
    }

    @Data
    @AllArgsConstructor
    private class WHKV{
        float airTemp;
        float airHumidity;
        float lightIntensity;  //klux
        float windVelocity;
        float windDirection;
        float dayrain;
    }

}
