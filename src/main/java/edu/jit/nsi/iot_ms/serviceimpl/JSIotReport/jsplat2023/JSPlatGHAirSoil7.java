package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil7 extends JSPlatJNSession {

    AirSoil7 data;

    public JSPlatGHAirSoil7(String tid, String sk, AirSoil7 as){
        super(tid,sk);
        data = new AirSoil7(as.getAirTemp(), as.getAirHumidity(), as.getSoilTemp(),
                as.getSoilMoisture(), as.getLightIntensity(), as.getDioxideCond(), as.getSoilsalt());
    }
}
