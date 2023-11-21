package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHSoil3 extends JSPlatJNSession {

    Soil3 data;

    public JSPlatGHSoil3(String tid, String sk, Soil3 s){
        super(tid,sk);
        data = new Soil3(s.getSoilTemp(), s.getSoilMoisture(), s.getElectroconductibility());
    }
}
