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
public class EnviHisSmryRsp extends EnviNewSmryRsp {
    public String type;
    public String time;  //时间
}
