package edu.jit.nsi.iot_ms.transport.msg;

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
public class EnviNewDtlRsp {
    public String name;//传感器名称
    public String type;
    public float value;//检测值
}
