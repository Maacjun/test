package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil6_2 extends JSPlatJNSession {

    AirSoil6_2 data;

    public JSPlatGHAirSoil6_2(String tid, String sk, AirSoil6_2 as){
        super(tid,sk);
        data = new AirSoil6_2(as.getAirTemp(), as.getAirHumidity(), as.getSoilTemp(),
                as.getSoilMoisture(), as.getLightIntensity(), as.getConductivity());
    }
}
