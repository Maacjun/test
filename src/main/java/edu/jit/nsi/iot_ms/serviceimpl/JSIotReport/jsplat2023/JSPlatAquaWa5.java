package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatAquaWa5 extends JSPlatJNSession {
    Water5 data;

    public JSPlatAquaWa5(String tid, String sk, Water5 other){
        super(tid,sk);
        data = new Water5(other.getDissolveO(), other.getPh(), other.getWaterTemp(),
                other.getElectroconductibility(),other.getTurbidity());
    }
}
