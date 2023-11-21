package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAir3 extends JSPlatJNSession {

    Air3 data;

    public JSPlatGHAir3(String tid, String sk, Air3 a3){
        super(tid,sk);
        data = new Air3(a3.getAirTemp(), a3.getAirHumidity(), a3.getLightIntensity());
    }

}
