package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil5 extends JSPlatJNSession {

    AirSoil5 data;

    public JSPlatGHAirSoil5(String tid, String sk, AirSoil5 as){
        super(tid,sk);
        data = new AirSoil5(as.getAirTemp(), as.getAirHumidity(), as.getSoilTemp(), as.getSoilMoisture(), as.getLightIntensity());
    }


}
