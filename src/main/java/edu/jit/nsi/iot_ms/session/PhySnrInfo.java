package edu.jit.nsi.iot_ms.session;

import edu.jit.nsi.iot_ms.config.SensorValue;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
public class PhySnrInfo {
    private int addr; //通道地址, 包括:485地址、LoRa通道
    private String product;
    private int reg;
    private int len;
    private List<SensorValue> rspvalue;

    public PhySnrInfo(int ad, String pro, int reg, int len, List<SensorValue> rspva){
        this.addr = ad;
        this.product=pro;
        this.reg=reg;
        this.len=len;
        this.rspvalue=rspva;
    }
}
