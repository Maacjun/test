package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JSIotDvcDef<T> {
    private  String groupname;
    private  List<Integer> termidlst;
    private  List<T> orglst;
}
