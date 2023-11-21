package edu.jit.nsi.iot_ms.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class TermType {
    int id;
    String type;
    String manu;
//    int dtcycle;
    Set<String> products;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String desc;
}
