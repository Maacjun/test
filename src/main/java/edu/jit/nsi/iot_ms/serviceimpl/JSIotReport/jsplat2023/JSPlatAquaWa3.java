package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatAquaWa3 extends JSPlatJNSession {
    Water3 data;

    public JSPlatAquaWa3(String tid, String sk, Water3 w3){
        super(tid,sk);
        data = new Water3(w3.getWaterOxygen(), w3.getPh(), w3.getWaterTemp());
    }
}
