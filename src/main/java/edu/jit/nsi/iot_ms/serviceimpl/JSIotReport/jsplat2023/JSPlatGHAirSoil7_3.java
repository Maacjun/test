package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil7_3 extends JSPlatJNSession {

    AirSoil7_3 data;

    public JSPlatGHAirSoil7_3(String tid, String sk, AirSoil7_3 as){
        super(tid,sk);
        data = new AirSoil7_3(as.getAirTemp(), as.getAirHumidity(), as.getSoilTemp(),
                as.getSoilMoisture(), as.getLightIntensity(), as.getDioxideCond(), as.getDewPoint());
    }
}
