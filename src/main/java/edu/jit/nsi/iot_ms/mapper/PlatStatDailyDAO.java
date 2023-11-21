package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.transport.msg.DailyTermStatus;
import edu.jit.nsi.iot_ms.transport.msg.EnviHisSmryRsp;
import edu.jit.nsi.iot_ms.transport.msg.HisTermStaAggr;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatStatDailyDAO extends BaseMapper<DailyTermStatus> {

    @Select("select DATE_FORMAT(time,'%Y-%m-%d') as date, min(volt) as minvolt, max(volt) as maxvolt, avg(volt) as avgvolt," +
            " sum(recvnum) as dailyrecvnum, sum(platnum) as dailyplatnum, avg(rpsec) as dailyrptsec " +
            "from platstat " +
            "where termid=#{termid} and time>#{start} and  time<#{end}" +
            "GROUP BY date")
    List<DailyTermStatus> getDailyTermSta(@Param("termid") Integer termid, @Param("start") String start, @Param("end") String end);

    @Select("<script>"+
            "select termid, name, recvsum,platsum,sucavg,rpsecavg,voltavg from termdef INNER JOIN ( " +
                "select termid, sum(recvnum) as recvsum, sum(platnum) as platsum, avg(sucrate) as sucavg, " +
                "avg(rpsec) as rpsecavg, avg(volt) as voltavg " +
                "from platstat " +
                "where <![CDATA[time>#{start} and time<#{end}]]> and termid in " +
                "<foreach collection='termids' item='item' open='(' close=')' separator=','>"+
                    "#{item}"+
                "</foreach>"+
                "GROUP BY termid " +
            ") as tsaggr " +
            "on termdef.id=tsaggr.termid"+
            "</script>")
    List<HisTermStaAggr> getHisTermStaAggr(@Param("termids") List<String> termids, @Param("start") String start, @Param("end") String end);

}
