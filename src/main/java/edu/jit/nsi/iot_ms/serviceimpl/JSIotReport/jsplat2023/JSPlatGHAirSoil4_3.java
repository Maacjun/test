package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023;

import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
public class JSPlatGHAirSoil4_3 extends JSPlatJNSession {

    AirSoil4_3 data;

    public JSPlatGHAirSoil4_3(String tid, String sk, AirSoil4_3 as){
        super(tid,sk);
        data = new AirSoil4_3(as.getAirTemp(), as.getAirHumidity(), as.getWaterPH(), as.getElectroconductibility());
    }
}
