package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatAquaWa2 extends JSPlatJNSession {
    Water2 data;

    public JSPlatAquaWa2(String tid, String sk, Water2 w2){
        super(tid,sk);
        data = new Water2(w2.getDissolveO(), w2.getPh());
    }
}
