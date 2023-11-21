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
@TableName("sensorphy")
public class SensorPhyDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private String product;
    private int termid;
    private int addr; //通道地址, 包括:485地址、LoRa通道
    private int cellid;
    private String name;

    public SensorPhyDO(String prod, int term_id, int addr, String sensor_name, int cell_id){
        product = prod;
        termid = term_id;
        this.addr = addr;
        cellid = cell_id;
        name = sensor_name;
    }
}
