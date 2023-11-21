package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil4_1 extends JSPlatJNSession {

    AirSoil4_1 data;

    public JSPlatGHAirSoil4_1(String tid, String sk, AirSoil4_1 dt){
        super(tid,sk);
        data = new AirSoil4_1(dt.getAirTemp(), dt.getAirHumidity(), dt.getSoilMoisture(),dt.getLightIntensity());
    }

}
