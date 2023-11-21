package edu.jit.nsi.iot_ms.transport.msg;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhrStnIlluIntgl {
    private String time;    //日期   2022-10-06
    float dailyTotalRad;  //总太阳辐射
    float dailyAvgRad;    //时均太阳辐射
    float peakHours;      //峰值日照小时数
    float dli;            //日光照积分DLI

    public void setRecords(float igtl){
        dailyTotalRad=(float)(igtl*0.0079/3.6);
        dailyAvgRad=(float)(dailyTotalRad/24);
        peakHours=(float)(dailyTotalRad/1000);
        dli=(float)(igtl*18/1000000);
    }
}
