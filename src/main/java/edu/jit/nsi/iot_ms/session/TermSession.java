package edu.jit.nsi.iot_ms.session;


import lombok.Data;
import org.apache.mina.core.session.IoSession;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TermSession {
    private int termid;
    private int termtype;
    private String deveui;
    private IoSession iosession;
    private List<Integer> sensorparams; //传感器参数列表
    private List<Integer> equiplst;  //设备列表
    private List<PhySnrInfo> physnrlst;
    private int toplat;    //0:不上报，1:上报省平台
    private float volt;      //电池电压
    private int recvnum;   //接收消息个数
    private int platnum;   //上报平台个数
    private int datacyc;   //配置的上报时间间隔
    private int rptsec;    //动态更新的上报间隔
    private boolean rptchg; //记录上报间隔是否发生变化,目前网关气象站未判断该字段直接ACK确认下发
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;       //数据上报时更新
    private Date intime;     //session创建时间

    public TermSession(int tid, int type, int cyc, int top, IoSession iosess){
        termid = tid;
        termtype = type;
        iosession = iosess;
        sensorparams = new ArrayList<Integer>();
        equiplst = new ArrayList<Integer>();
        toplat=top;
        volt=(float) 0.0;
        recvnum=0;
        platnum=0;
        datacyc=cyc;
        rptsec=cyc;
        rptchg =false;
        time = new Date();
        intime = new Date();
    }

    public TermSession(int tid, int type, int cyc, int top, String eui){
        termid = tid;
        termtype = type;
        deveui = eui;
        sensorparams = new ArrayList<Integer>();
        equiplst = new ArrayList<Integer>();
        toplat=top;
        volt=(float) 0.0;
        recvnum=0;
        platnum=0;
        datacyc=cyc;
        rptsec=cyc;
        rptchg =false;
        time = new Date();
        intime = new Date();
        if(type==5){
            physnrlst = new ArrayList<>();
        }
    }

    public void incrCounter(boolean succfg){
        recvnum++;
        if(succfg)
            platnum++;
    }

    public int incrRptSec(){
        if(rptsec+60<=datacyc*2)
            rptsec+=60;
        return rptsec;
    }

    public void decRptSec(){
        if(rptsec-60>datacyc*1.5)
            rptsec=(int)(datacyc*1.5);
        else if(rptsec-60>datacyc)
            rptsec-=60;
        else
            rptsec=datacyc;
    }

    public void updtVolt(float vo){
        volt=vo;
    }

    public void addPhysnrlst(PhySnrInfo p) {
        physnrlst.add(p);
    }
}
