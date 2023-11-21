package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @className: CellDO
 * @author: kay
 * @date: 2019/7/26 9:18
 * @packageName: com.jit.iot.jit.edu.nsi.domain
 */

@Data
@AllArgsConstructor
@TableName("loranwkdata")
public class LoRaNwkDO {
    @TableId(type = IdType.AUTO)
    private int id;            //主键id
    private String deveui;     //设备串号
    private String devaddr;    //短地址
    private int frequency;     //发送频率
    private  int dr;           //regional param 0:sf12, 1:sf11,  2:sf10, 3:sf9, 4:sf8, 5:sf7
    private boolean adr;       //是否支持ADR
    private  int fcnt;         //消息计数器
    private  int fport;        //端口号

    private boolean dup;       //消息重复，由多基站接收
    private String gwid;  //接收基站ID
    private int rssi;          //接收信号强度
    private float snr;     //接收信噪比

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;


    public LoRaNwkDO(String eui, String addr, int frq, int dr,
                     boolean adr, int cnt, int port, boolean dupl, String gwID, int rssi, float snr, Date dt){
        deveui = eui;
        devaddr = addr;
        frequency = frq;
        this.dr = dr;
        this.adr = adr;
        fcnt = cnt;
        fport = port;
        dup = dupl;
        gwid = gwID;
        this.rssi = rssi;
        snr= snr;
        time = dt;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Date getTime(){
        return time;
    }
}
