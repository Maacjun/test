package edu.jit.nsi.iot_ms.session;

import edu.jit.nsi.iot_ms.domain.EquipDO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class EquipSession{
    private int id;
    private String type;
    private String defname;

    private int termid;
    private int addr;
    private int road;

    private int cellid;
    private int status; //当前状态 0:关   1:开
    private boolean active; //是否上报状态，继电器是否活着
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public EquipSession(EquipDO eq){
        id = eq.getId();
        type = eq.getType();
        defname = eq.getDefname();
        termid = eq.getTermid();
        addr = eq.getAddr();
        road = eq.getRoad();
        cellid = eq.getCellid();
        status = 0;
        time = new Date();
    }
}
