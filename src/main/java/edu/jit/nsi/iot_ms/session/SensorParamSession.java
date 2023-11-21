package edu.jit.nsi.iot_ms.session;


import edu.jit.nsi.iot_ms.domain.SensorParamDO;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class SensorParamSession {
    private int paramid;
    private int phyid;
    private int cellid;
    private int termid;
    private String  defname;
    private int addr;
    private int reg;
    private String type;
    private float value;
    private boolean active;  //是否在线,上报数据
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public SensorParamSession(SensorPhyDO phyDO, SensorParamDO paramDO){
        //取配置
        paramid =paramDO.getId();
        phyid = phyDO.getId();
        cellid = phyDO.getCellid();
        termid = phyDO.getTermid();
        defname = phyDO.getName();
        addr = phyDO.getAddr();

        reg = paramDO.getReg();
        type = paramDO.getParam();
        value=(float) 0.0;
        active = false;
        time = new Date();
    }
}
