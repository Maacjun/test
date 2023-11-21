package edu.jit.nsi.iot_ms.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvThrsdParam {
    private String env;
    private float wnup;
    private float wndw;
    private float actup;
    private float actdw;
}
