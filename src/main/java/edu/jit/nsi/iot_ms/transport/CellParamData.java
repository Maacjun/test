package edu.jit.nsi.iot_ms.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: ReportData
 * @author: kay
 * @date: 2019/7/22 14:59
 * @packageName: com.jit.iot.utils
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellParamData {
    public int snpid;//传感器采集参数id
    public String type;//传感器采集参数类型
    public String typeCHN;//传感器采集参数类型的中文名字
    public String suffix;
    public String name;//自定义传感器名称,使用物理实体传感器名称

    public CellParamData(int id, String t, String n){
        snpid = id;
        type = t;
        name=n;
    }
}
