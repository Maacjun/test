package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.EnvirDataDO;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CellEnvDAO extends BaseMapper<SensorPhyDO> {
    @Select("select * from envirdata inner join sensorphy" +
            "on envirdata.termid=sensorphy.termid and envirdata.addr=sensorphy.addr and sensorphy.cellid=#{cid}")
    List<EnvirDataDO> cellEnvir(@Param("cid") Integer cid);
}
