package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.util.Date;

/**
 * @className: Sensor
 * @author: kay
 * @date: 2019/7/26 9:18
 * @packageName: com.jit.iot.jit.edu.nsi.domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("platstat")
public class PlatStatDO {
    @TableId(type = IdType.AUTO)
    private BigInteger id;
    private int termid;
    private int rpsec;   //当前的上报周期
    private int recvnum;
    private int platnum;
    private float sucrate;
    private float volt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public PlatStatDO(int tid, int rs, int rv, int pl, float su, float vo, Date t){
        termid = tid;
        rpsec=rs;
        recvnum = rv;
        platnum = pl;
        sucrate = su;
        volt=vo;
        time = t;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Date getTime(){
        return time;
    }
}
