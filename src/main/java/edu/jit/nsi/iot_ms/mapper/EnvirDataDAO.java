package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.EnvirDataDO;
import edu.jit.nsi.iot_ms.transport.msg.EnviHisDtlRsp;
import edu.jit.nsi.iot_ms.transport.msg.EnviHisSmryRsp;
import edu.jit.nsi.iot_ms.transport.msg.EnviParamExp;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvirDataDAO extends BaseMapper<EnvirDataDO> {
    @Insert({"<script>" +
            "insert into envirdata(`termid`, `snpid`, `addr`, `reg`, `type`, `value`, `time`) values" +
            "<foreach collection=\"list\" item=\"item\" separator=\",\">" +
            "(#{item.termid}, #{item.snpid}, #{item.addr}, #{item.reg}, #{item.type}, #{item.value}, #{item.time})" +
            "</foreach>" +
            "</script>"})
    void insertBatch(List<EnvirDataDO> envirDataDOList);

    @Select("select ev.snpid, ev.value, ev.type, ev.time from envirdata ev inner join sensorphy sp on " +
            "ev.`termid`=sp.`termid` and ev.`addr`=sp.`addr` " +
            "and sp.`cellid`=#{cid} and ev.time>#{start} and ev.time<#{end} and ev.`type`!='RELAY_DMA'")
    List<EnviHisSmryRsp> cellHisSmryEnvir(@Param("cid") Integer cid, @Param("start") String start, @Param("end") String end);

    @Select("select sd.name, ev.type, ev.value, ev.time from envirdata ev inner join sensorphy sd on " +
            "ev.`termid`=sd.`termid` and ev.`addr`=sd.`addr` " +
            "and sd.`cellid`=#{cid} and ev.time>#{start} and ev.time<#{end}  and ev.`type`!='RELAY_DMA'")
    List<EnviHisDtlRsp> cellHisDtlEnvir(@Param("cid") Integer cid, @Param("start") String start, @Param("end") String end);

    @Select("<script>"+
            "select ev.time, ev.type, ev.value from envirdata ev where ev.termid = #{tid} and <![CDATA[ev.time>#{start} and ev.time<#{end}]]> and ev.type in " +
            "<foreach collection=\"tplist\" item=\"item\" index=\"index\" open=\"(\" close=\")\" separator=\",\">"+
            "#{item}"+
            "</foreach>"+
            "</script>")
    List<EnviParamExp> exportParam(@Param("tid") Integer tid, @Param("start") String start, @Param("end") String end,  @Param("tplist") List<String> tplist);
}

