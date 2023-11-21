package edu.jit.nsi.iot_ms.transport.msg;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HisTermStaAggr {
    private int termid;
    private String name;    //终端名字
    private int recvsum;   //总接收消息个数
    private int platsum;   //总上报平台个数
    private float sucavg;    //平均上报成功率
    private float rpsecavg;    //平均上报间隔
    private float voltavg;    //平均电压
}
