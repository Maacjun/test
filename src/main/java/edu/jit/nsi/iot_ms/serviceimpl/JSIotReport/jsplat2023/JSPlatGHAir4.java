package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAir4 extends JSPlatJNSession {
    Air4 data;

    public JSPlatGHAir4(String tid, String sk, Air4 a4){
        super(tid,sk);
        data = new Air4(a4.getAirTemp(), a4.getAirHumidity(), a4.getLightIntensity(), a4.getDioxideCond());
    }
}
