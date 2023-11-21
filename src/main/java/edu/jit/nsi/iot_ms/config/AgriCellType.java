package edu.jit.nsi.iot_ms.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AgriCellType {
    int id;
    String celltype;
    List<String> agprods;
    String desc;
}
