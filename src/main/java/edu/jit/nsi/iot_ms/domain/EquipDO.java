package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @packageName: com.jit.iot.entry
 * @className: Equipment
 * @Description:
 * @author: xxz
 * @date: 2019/7/30 19:01
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("equipdef")

public class EquipDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private int cellid;
    private String type;

    private int termid;
    private int addr;
    private int road;
    private String defname;

    public EquipDO(Integer eq_id){
        id = eq_id;
    }

    public EquipDO(String defname, String type, int termid, int addr, int rd, int cid){
        this.defname = defname;
        this.type = type;
        this.termid = termid;
        this.addr = addr;
        road = rd;
        cellid = cid;
    }
}
