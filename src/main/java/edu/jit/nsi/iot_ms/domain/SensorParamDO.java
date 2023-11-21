package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @className: Sensor
 * @author: kay
 * @date: 2019/7/26 9:18
 * @packageName: com.jit.iot.jit.edu.nsi.domain
 */
@Data
@AllArgsConstructor
@TableName("sensorparam")
public class SensorParamDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private int reg;
    private String param;
    private int phyid;
    public SensorParamDO(int reg, String param, int phyid){
        this.reg = reg;
        this.param = param;
        this.phyid = phyid;
    }
}
