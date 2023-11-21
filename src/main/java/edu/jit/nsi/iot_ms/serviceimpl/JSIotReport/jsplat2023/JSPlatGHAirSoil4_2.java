package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil4_2 extends JSPlatJNSession {

    AirSoil4_2 data;

    public JSPlatGHAirSoil4_2(String tid, String sk, AirSoil4_2 as){
        super(tid,sk);
        data = new AirSoil4_2(as.getAirTemp(), as.getAirHumidity(), as.getSoilTemp(), as.getLightIntensity());
    }
}
