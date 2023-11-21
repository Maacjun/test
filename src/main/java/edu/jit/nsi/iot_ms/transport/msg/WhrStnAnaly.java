package edu.jit.nsi.iot_ms.transport.msg;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhrStnAnaly {
    private String time;    //数据上报时间
    private int cnt;        //当日数据个数
    private float min;      //最小值
    private float mean;     //平均值
    private float max;      //最大值
    private float stddev;   //标准差
}
