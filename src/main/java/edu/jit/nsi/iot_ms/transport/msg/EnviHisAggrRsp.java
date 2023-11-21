package edu.jit.nsi.iot_ms.transport.msg;

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
public class EnviHisAggrRsp {
    public String type;
    public List<SNTV> sntvs;

    public EnviHisAggrRsp(String tp){
        type = tp;
        sntvs = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class SNTV {
        //public String name;//传感器名称
        public int snpid;
        public List<Long> times;
        public List<Float> values;  //时间

        public SNTV(int n){
            snpid = n;
            times = new ArrayList<>();
            values = new ArrayList<>();
        }
    }
}
