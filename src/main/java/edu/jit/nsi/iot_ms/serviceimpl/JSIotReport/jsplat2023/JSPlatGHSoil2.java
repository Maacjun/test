package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHSoil2 extends JSPlatJNSession {

    Soil2 data;

    public JSPlatGHSoil2(String tid, String sk, Soil2 s){
        super(tid,sk);
        data = new Soil2(s.getSoilTemp(), s.getSoilMoisture());
    }
}
