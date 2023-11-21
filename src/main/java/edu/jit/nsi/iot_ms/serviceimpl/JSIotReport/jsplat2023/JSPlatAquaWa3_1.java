package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatAquaWa3_1 extends JSPlatJNSession {
    Water3_1 data;

    public JSPlatAquaWa3_1(String tid, String sk, Water3_1 other){
        super(tid,sk);
        data = new Water3_1(other.getDissolveO(), other.getPh(), other.getWaterTemp());
    }
}
