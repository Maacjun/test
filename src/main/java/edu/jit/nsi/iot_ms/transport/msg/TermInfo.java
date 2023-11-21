package edu.jit.nsi.iot_ms.transport.msg;

import edu.jit.nsi.iot_ms.domain.CellDO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @className: CellDO
 * @author: kay
 * @date: 2019/7/26 9:18
 * @packageName: com.jit.iot.jit.edu.nsi.domain
 */
@Data
@AllArgsConstructor
public class TermInfo {
    private int termid;
    private double longitude;
    private double latitude;
    private String type;
    private String product;
    private String name;
    private String username;

    public TermInfo(int tid){
        termid = tid;
    }

    public void setAllInfo(CellDO cdo){
        longitude = cdo.getLongitude();
        latitude = cdo.getLatitude();
        type = cdo.getType();
        product = cdo.getProduct();
        name = cdo.getName();
        username = cdo.getUsername();
    }
}
