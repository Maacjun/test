import edu.jit.nsi.iot_ms.Fegin.SMS.SMSFeignClient;
import edu.jit.nsi.iot_ms.IoTApplication;
import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.JSIotJNDataModel;
import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023.*;
import edu.jit.nsi.iot_ms.transport.ReportData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

@Slf4j
public class unitTestMain {

//    public static void main(String[] args) {
//        JSIotJNDataModel model = new JSIotJNDataModel();
//        ReportData datatmp = new ReportData(1,1,"temp",22.9f);
//        ReportData datahumi = new ReportData(1,2,"humi",52.3f);
//        ReportData dataco2 = new ReportData(1,2,"co2",128.0f);
//        ReportData dataillu = new ReportData(1,2,"illu",28987.0f);
//
//        ReportData dataST = new ReportData(1,2,"soilTemp",19.1f);
//        ReportData dataSH = new ReportData(1,2,"soilHumi",100.0f);
//        ReportData dataSEC = new ReportData(1,2,"soilEC",500.0f);
//
//        model.updtModel(135, datatmp);
//        model.updtModel(135, datahumi);
//        model.updtModel(144, dataillu);//illu
//        model.updtModel(175, dataco2);//co2
//
//        model.updtModel(205, dataST);//soilTemp
//        model.updtModel(205, dataSH);//soilHumi
//        model.updtModel(205, dataSEC);//soilEC
//
//        Air4 air4 = model.constructAir4(0.05f);
//        AirSoil4_1 airSoil4_1 = model.constructAS4_1(0.05f);
//        AirSoil6_2 as6_2 = model.constructAS6_2(0.05f);
//        AirSoil7_2 as7_2 = model.constructAS7_2(0.05f);
//
//        AirSoil7 as7= model.constructAS7(0.05f);
//
//        log.info("Air4:{} ",air4.toString());
//        log.info("AirSoil4_1:{} ",airSoil4_1.toString());
//        log.info("AirSoil6_2:{} ",as6_2.toString());
//        log.info("AirSoil7_2:{} ",as7_2.toString());
//        log.info("AirSoil7_2:{} ",as7.toString());
//    }
//    @Autowired
//    SMSFeignClient smsFeignClient;
//    @Test
//    public void SmsTest(){
//        smsFeignClient.warnMessage(1,"塘口","do",1.2f,"1","17714429653");
//
//    }
}
