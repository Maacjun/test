package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatJCCo2Data extends JSPlatJNSession {
    CO2KV data;

    public JSPlatJCCo2Data(String tid, String sk, float co2){
        super(tid,sk);
        data = new CO2KV(co2);
    }
    @Data
    @AllArgsConstructor
    private class CO2KV {
        float dioxideCond;
    }
}
