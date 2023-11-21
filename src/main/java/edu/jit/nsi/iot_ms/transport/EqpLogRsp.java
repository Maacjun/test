package edu.jit.nsi.iot_ms.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: ReportData
 * @author: kay
 * @date: 2019/7/22 14:59
 * @packageName: com.jit.iot.utils
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqpLogRsp{
    public int eqpid;
    public int ondurt;
    public int offdurt;
    public String eqpname;
    public List<Byte> optlist;
    public List<Long> timelist;

    public EqpLogRsp(int epid, String name){
        eqpid = epid;
        eqpname = name;
        ondurt = 0;
        offdurt = 0;
        optlist = new ArrayList();
        timelist = new ArrayList();
    }
    public void incrOnTm(int dur){
        ondurt+=dur;
    }
    public void incrOffTm(int dur){
        offdurt+=dur;
    }
}


