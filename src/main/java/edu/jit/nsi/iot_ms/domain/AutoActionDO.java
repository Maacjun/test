package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@TableName("autolog")
public class AutoActionDO {
    private int id;
    private int cellid;
    private String param;
    private int opt;  //0:关闭  1:打开
    private float value;
    private Date time;

    public AutoActionDO(int cid, String ptype, int op, float va, Date time) {
        this.cellid = cid;
        this.param = ptype;
        this.opt = op;
        this.value = va;
        this.time = time;
    }

    public AutoActionDO(long id, int cid, String ptype, int op, float va, Timestamp ts) {
        this.id = (int)id;
        this.cellid = cid;
        this.param = ptype;
        this.opt = op;
        this.value = va;
        this.time = ts;
    }
}
