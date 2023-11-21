package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("envtmctldef")
public class EnvTimeCtlDO {
    @TableId(type = IdType.AUTO)
    private int id;       //主键ID
    private int cellid;   //塘口id
    private String param;  //环境参数类型
//    @DateTimeFormat(pattern = "HH:mm:ss")
//    private Date time;  //动作时间
    private String time;
    private int opt;  //执行动作
    private int autofg;    //自动开关
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public EnvTimeCtlDO(int cellid, String param, String time, int opt, int autofg){
        this.cellid=cellid;
        this.param=param;
//        try {
//            this.time = sdf.parse(time);
//        }catch (Exception e){
//            log.error("format time{} to Date error",time);
//        }
        this.time = time;
        this.opt = opt;
        this.autofg=autofg;
    }

//    public EnvTimeCtlDO(int id, int cellid, String param, String time, int opt, int autofg){
//        this.cellid=cellid;
//        this.param=param;
//        this.time = time;
//        this.opt = opt;
//        this.autofg=autofg;
//    }

//    public void setTime(String time){
//        try {
//            this.time = sdf.parse(time);
//        }catch (Exception e){
//            log.error("format time{} to Date error",time);
//        }
//    }
}
