package edu.jit.nsi.iot_ms.transport.tcp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @className: ReportData
 * @author: kay
 * @date: 2019/7/22 14:59
 * @packageName: com.jit.iot.utils
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipNewRsp {
    public int epid;//传感器名称
    public String name;//传感器名称
    public String type;//传感器类型
    public int status;  //0:关闭 或者 1:打开
    public Date time;//设备操作时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Date getTime(){
        return time;
    }
}
