package edu.jit.nsi.iot_ms.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvParamDesc {
    private String env;
    private String suffix;
    private boolean to_usr;
    private String desc;
}
