package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
@TableName("sendmessage")
public class sendmessage {
    @TableId(type = IdType.AUTO)
    int id;
    int cellid;
    String param;
    float value;
    Timestamp time;
    String phone;
    public sendmessage(int cellid, String param, float value, Timestamp time,String phone) {
        this.cellid = cellid;
        this.param = param;
        this.value = value;
        this.time = time;
        this.phone=phone;
    }
}
