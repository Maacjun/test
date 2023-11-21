package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.SensorParamDO;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorParamDAO extends BaseMapper<SensorParamDO> {
}
