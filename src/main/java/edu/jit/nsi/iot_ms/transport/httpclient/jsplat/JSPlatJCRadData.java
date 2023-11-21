package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatJCRadData extends JSPlatJNSession {
    RadKV data;

    public JSPlatJCRadData(String tid, String sk, float rr){
        super(tid,sk);
        data = new RadKV(rr);
    }
    @Data
    @AllArgsConstructor
    private class RadKV{
        float Irradiance;
    }
}
