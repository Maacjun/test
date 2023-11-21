package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatAquaWa4 extends JSPlatJNSession {
    Water4 data;

    public JSPlatAquaWa4(String tid, String sk, Water4 w4){
        super(tid,sk);
        data = new Water4(w4.getDissolveO(), w4.getPh(), w4.getWaterTemp(), w4.getConductivity());
    }
}
