package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: Sensor
 * @author: kay
 * @date: 2019/7/26 9:18
 * @packageName: com.jit.iot.jit.edu.nsi.domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("termdef")
public class TermDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private int type;       // 1:工控机485, 2:矽递, 3:北京农芯终端, 4:易通
    private String deveui ; //seeed和农芯的设备串号
    private String name;    //设备自定义名称
    private String username;
    private String manu;
    private String product;
    private int datacycle;
    private int preheat;
    private int toplat;

    public TermDO(int term_type, String user, String term_name, String man, String prod, int dt, int ph, int plat){
        type = term_type;
        deveui = null;
        username = user;
        name = term_name;
        manu = man;
        product = prod;
        datacycle = dt;
        preheat = ph;
        toplat=plat;
    }

    public TermDO(int term_type, String dev_eui, String user, String term_name, String man, String prod, int dt, int ph, int plat){
        type = term_type;
        deveui = dev_eui;
        username = user;
        name = term_name;
        manu = man;
        product = prod;
        datacycle = dt;
        preheat = ph;
        toplat=plat;
    }
}
