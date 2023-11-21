package edu.jit.nsi.iot_ms.domain;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
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
@TableName("cellinf")
public class CellDO {
    @TableId(type = IdType.AUTO)
    private int id;
    private float  length;
    private float width;
    private double longitude;
    private double latitude;
    private String type;
    private String product;
    private String name;
    private String username;

    public CellDO(int cid){
        id = cid;
    }

    public CellDO(float len, float wd, double lon, double lat, String tp, String pd, String nm, String usr_name){
        length = len;
        width = wd;
        longitude = lon;
        latitude = lat;
        type = tp;
        product = pd;
        name = nm;
        username = usr_name;
    }
}
