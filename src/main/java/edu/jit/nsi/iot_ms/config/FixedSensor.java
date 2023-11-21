package edu.jit.nsi.iot_ms.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FixedSensor<T> {
    private String product;
    private int manuid;
    private String desc;
    private List<T> params;
}
