package edu.jit.nsi.iot_ms.transport.tcp.payload;

import edu.jit.nsi.iot_ms.transport.ReportData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @className: CommonReplyData
 * @author: kay
 * @date: 2019/7/22 15:58
 * @packageName: com.jit.iot.utils.hardware
 */
@Data
@AllArgsConstructor
public class CommonReplyData {
    private String terminal;
    private String msgType;
    private int id;
    private List<ReportData> content;
    private String result;
}
