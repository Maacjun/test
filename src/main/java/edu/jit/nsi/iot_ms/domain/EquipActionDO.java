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
@TableName("equipaction")
public class EquipActionDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private int equipid;
    private byte onofflg;
    private byte ctlmode;  //0:by app , 1:by hand, 2:by computer

    private Date time;

    public EquipActionDO(int eqid, byte onofflg, byte isman,  Date time) {
        this.equipid = eqid;
        this.onofflg = onofflg;
        this.ctlmode = isman;
        this.time = time;
    }

    public EquipActionDO(long sid, int eqid, int onff, int ctl, Timestamp ts){
        id=(int)sid;
        equipid=eqid;
        onofflg=(byte)onff;
        ctlmode=(byte)ctl;
        time=ts;
    }
}
