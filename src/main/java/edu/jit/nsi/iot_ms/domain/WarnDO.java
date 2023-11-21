package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@TableName("warnlog")
public class WarnDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private int cellid;
    private String param;
    private boolean isdown;
    private int sta;  //-1：关闭  0:无操作（告警）  1:打开
    private float value;
    private Date time;

    public WarnDO(int cid, String ptype, boolean down, int sa, float va, Date time) {
        this.cellid = cid;
        this.param = ptype;
        this.isdown = down;
        this.sta = sa;
        this.value = va;
        this.time = time;
    }

    public WarnDO(long sid, int cid, String ptype, boolean down, int sa, float va, Timestamp ts){
        id=(int)sid;
        cellid=cid;
        param = ptype;
        isdown = down;
        sta =sa;
        value = va;
        time=ts;
    }
}
