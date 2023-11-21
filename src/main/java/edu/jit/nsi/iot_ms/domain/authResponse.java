package edu.jit.nsi.iot_ms.domain;

import lombok.Data;

@Data
public class authResponse {
    private Integer code;
    private String msg;
    private Object data;
}
