package edu.jit.nsi.iot_ms.transport.httpclient.jsplat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JSPlatRptRes {
    int code;
    String desc;
    Object data;
}
