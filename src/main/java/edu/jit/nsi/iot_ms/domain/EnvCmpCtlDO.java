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
@TableName("envcmpctldef")
public class EnvCmpCtlDO {
    @TableId(type = IdType.AUTO)
    private int id;       //主键ID
    private int cellid;   //塘口id
    private String param;  //环境参数类型
    private int paramid1;  //判断比较传感器1参数id
    private int paramid2;  //判断比较传感器2参数id
    private boolean grthn; //是否大于 1:大于 0:小于
    private float dist;    //差值
    private int opt;       //执行动作 0:关，1:开
    private int autofg;    //自动开关

    public EnvCmpCtlDO(int cellid, String param, int paramid1,int paramid2,boolean grthn,
                       float dist, int opt, int autofg){
        this.cellid=cellid;
        this.param=param;
        this.paramid1=paramid1;
        this.paramid2=paramid2;
        this.grthn=grthn;
        this.dist=dist;
        this.opt=opt;
        this.autofg=autofg;
    }

}
