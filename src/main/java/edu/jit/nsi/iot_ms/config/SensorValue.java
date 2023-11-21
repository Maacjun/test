package edu.jit.nsi.iot_ms.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @className: RepValuesJson
 * @author: kay
 * @date: 2019/7/25 14:29
 * @packageName: com.jit.iot.utils.json
 */
@Data
@NoArgsConstructor
public class SensorValue {
    private  String stype;
    private List<Integer> value;
    private  Integer unit;
}
