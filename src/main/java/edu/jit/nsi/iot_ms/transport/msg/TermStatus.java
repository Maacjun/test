package edu.jit.nsi.iot_ms.transport.msg;


import edu.jit.nsi.iot_ms.session.PhySnrInfo;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.session.TermSession;
import lombok.Data;
import org.apache.mina.core.session.IoSession;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TermStatus {
    private int termid;
    private int termtype;
    private String deveui;

    private float volt;      //电池电压
    private int recvnum;   //接收消息个数
    private int platnum;   //上报平台个数
    private int datacyc;   //配置的上报时间间隔
    private int rptsec;    //动态更新的上报间隔

    private Date time;    //数据上报时间
    private Date intime;  //session 创建时间

    private List<SessionManager.SnPhySta> phyStas; //物理传感器状态列表

    public TermStatus(TermSession ts){
        termid = ts.getTermid();
        deveui=ts.getDeveui();
        termtype = ts.getTermtype();
        volt=ts.getVolt();
        recvnum=ts.getRecvnum();
        platnum=ts.getPlatnum();
        datacyc=ts.getDatacyc();
        rptsec=ts.getRptsec();
        time = ts.getTime();
        intime = ts.getIntime();
    }
}
