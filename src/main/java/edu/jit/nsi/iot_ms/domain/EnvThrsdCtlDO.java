package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("envthctldef")
public class EnvThrsdCtlDO {
    @TableId(type = IdType.AUTO)
    private int id;       //主键ID
    private int cellid;   //塘口id
    private String param;  //环境参数类型
    private int paramid;  //绑定的传感器参数id
    private float wnup;   //告警上限
    private float wndw;   //告警下限
    private float actup;  //控制上限
    private float actdw;  //控制下限
//    private float stopftor; //设备关闭比例因子,相对告警
//    private int actupeqid; //操作上限的设备
//    private int actdweqid; //操作下限的设备
    private int autofg;    //自动开关

    public EnvThrsdCtlDO(int cellid, String param, int paramid,
                         float wnup, float wndw, float actup, float actdw, int autofg){
//            int actupeqid, int actdweqid,

        this.cellid=cellid;
        this.param=param;
        this.paramid=paramid;
        this.wnup = wnup;
        this.wndw = wndw;
        this.actup=actup;
        this.actdw=actdw;
//        this.actupeqid=actupeqid;
//        this.actdweqid=actdweqid;
        this.autofg=autofg;
    }


}
