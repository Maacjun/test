package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import edu.jit.nsi.iot_ms.transport.ReportData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@TableName("envirdata")
public class EnvirDataDO extends ReportData {
    @TableId(type = IdType.AUTO)
    private BigInteger id;
    private int termid;
    private int snpid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    //数据库查询历史数据
    public EnvirDataDO(Integer tId, Integer pId, Integer addr, Integer reg, String type, Float value, Timestamp time) {
        this.termid = tId;
        this.snpid = pId;
        this.addr = addr;
        this.reg  = reg;
        this.type = type;
        this.value = value;
        this.time = time;
    }

    public EnvirDataDO(Integer tId, Integer pId, Integer addr, Integer reg, String type, Float value, Date time) {
        this.termid = tId;
        this.snpid = pId;
        this.addr = addr;
        this.reg  = reg;
        this.type = type;
        this.value = value;
        this.time = time;
    }

    public EnvirDataDO(BigInteger id, Integer gwId, Integer pId, Integer addr,Integer reg, String type, Float value, Timestamp time) {
        this.id = id;
        this.termid = gwId;
        this.snpid = pId;
        this.addr = addr;
        this.reg  = reg;
        this.type = type;
        this.value = value;
        this.time = time;
    }

    public EnvirDataDO(BigInteger id, Integer gwId, Integer pId, Timestamp time, Integer addr,Integer reg, String type, Float value) {
        this.id = id;
        this.termid = gwId;
        this.snpid = pId;
        this.addr = addr;
        this.reg  = reg;
        this.type = type;
        this.value = value;
        this.time = time;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Date getTime(){
        return time;
    }
}
