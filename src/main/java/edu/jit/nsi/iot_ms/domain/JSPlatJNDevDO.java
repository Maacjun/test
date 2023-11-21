package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("jndevstat")
public class JSPlatJNDevDO {
    private static int FIXSCHE = 930;   //配置的上报时间间隔
    @TableId(type = IdType.AUTO)
    private int id;       //主键ID
    private String deviceid;
    private String ownername;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lstime;
    private int rptnum;
    private int succnum;
    private int prdsec;   //单位秒
    private float stddist;

    public JSPlatJNDevDO(String dvid, String owner, Date lst, int p, float dist){
        deviceid=dvid;
        ownername=owner;
        rptnum=0;
        succnum=0;
        lstime=lst;
        prdsec=p;
        stddist=dist;
    }

    public boolean isAction(){
        long now = System.currentTimeMillis();
        /*与上次上报间隔超过period sec, 同时满足间隔小于3小时(防止传感器停掉报重复数据)*/
        if(now - lstime.getTime()>prdsec*1000 && now - lstime.getTime()<60*60*3*1000){
            return true;
        }else{
            return false;
        }
    }

    public void incrMsgNum(boolean succfg){
        rptnum++;
        if(succfg)
            succnum++;
    }


    public void incrMsgPeriod(boolean succfg){
        if(!succfg)
            prdsec+=30;
    }

    public void resetNum(){
        rptnum=0;
        succnum=0;
    }

    public void adjustMsgPeriod(){
        float sucr=0;
        if(rptnum!=0)
            sucr = (float) succnum/rptnum;

        if (sucr > 0.9 && prdsec > FIXSCHE*1.3) {
            if(prdsec-30 > FIXSCHE*1.5)
                prdsec=(int)(FIXSCHE*1.5);
            else if(prdsec-30 > FIXSCHE)
                prdsec-=30;
            else
                prdsec=FIXSCHE;
        }
    }
}
