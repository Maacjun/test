package edu.jit.nsi.iot_ms.transport.msg;


import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.session.TermSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTermStatus {
    private String date;    //数据上报时间
    private float minvolt;      //最小电池电压
    private float maxvolt;      //最大电池电压
    private float avgvolt;      //平均电池电压

    private int dailyrecvnum;   //平均每日总接收消息个数
    private int dailyplatnum;   //平均每日上报平台个数
    private int dailyrptsec;    //平均每日上报间隔
}
