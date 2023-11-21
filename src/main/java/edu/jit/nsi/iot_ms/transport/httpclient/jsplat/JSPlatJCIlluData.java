package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatJCIlluData extends JSPlatJNSession {
    IlluKV data;

    public JSPlatJCIlluData(String tid, String sk, float illu){
        super(tid,sk);
        data = new IlluKV(illu);
    }
    @Data
    @AllArgsConstructor
    private class IlluKV{
        float lightIntensity;
    }
}
