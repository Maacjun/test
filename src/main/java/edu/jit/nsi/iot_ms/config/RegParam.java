package edu.jit.nsi.iot_ms.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegParam {
    private int reg;
    private int unit;
    private String envparam;
}
