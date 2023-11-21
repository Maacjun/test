package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import edu.jit.nsi.iot_ms.serviceimpl.impl.AppServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorPhyDAO extends BaseMapper<SensorPhyDO> {
    @Select("select pa.param, pa.id, td.datacycle from sensorphy phy inner join sensorparam pa inner join termdef td on " +
            "phy.id=pa.phyid and phy.termid=td.id and phy.cellid=#{cellid}")
    List<AppServiceImpl.ParamID> paramsInCell(@Param("cellid") int cellid);

    @Select("select pa.id from sensorphy phy inner join sensorparam pa on " +
            "phy.id=pa.phyid and phy.termid=#{termid} and pa.param=#{param}")
    List<Integer> paramsInTermType(@Param("termid") int termid, @Param("param")String param);

    @Select("<script>"+
            "select pa.param, pa.id, td.datacycle from sensorphy phy inner join sensorparam pa inner join termdef td on " +
            "phy.id=pa.phyid and phy.termid=td.id and phy.cellid in " +
            "<foreach collection=\"cells\" item=\"item\" index=\"index\" open='(' separator=',' close=')'>" +
            "${item}" +
            "</foreach>"+
            "and pa.param in "+
            "<foreach collection=\"types\" item=\"item\" index=\"index\" open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>"+
            "</script>")
    List<AppServiceImpl.ParamID> phySnByCellType(@Param("cells") List<Integer> cells, @Param("types") List<String> types);
}