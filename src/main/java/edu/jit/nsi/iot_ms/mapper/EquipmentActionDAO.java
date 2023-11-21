package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.EquipActionDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentActionDAO extends BaseMapper<EquipActionDO> {
    @Select("select ec.id, ec.equipid, ec.onofflg, ec.ctlmode, ec.time  from equipaction ec inner join equipdef ed on " +
            "ec.equipid=ed.id and ed.cellid=#{cellid} and ec.time>#{start} and ec.time<#{end}")
    List<EquipActionDO> equipActLog(@Param("cellid") int cellid, @Param("start") String start, @Param("end") String end);
}
