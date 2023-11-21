package edu.jit.nsi.iot_ms.serviceimpl.JSIotReport;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JNAgriOrg {
    private  String orgname;
    private  List<String> deviceidlst;
}
