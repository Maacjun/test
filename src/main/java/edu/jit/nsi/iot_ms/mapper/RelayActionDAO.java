package edu.jit.nsi.iot_ms.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import edu.jit.nsi.iot_ms.domain.RelayActionDO;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelayActionDAO extends BaseMapper<RelayActionDO> {
    @Insert({"<script>" +
            "insert into relayaction(`equipid`, `cellid`, `termid`, `addr`, `road`, `onofflg`, `ctlmode`, `value`, `time`) values" +
            "<foreach collection=\"list\" item=\"item\" separator=\",\">" +
            "( #{item.equipid}, #{item.cellid}, #{item.termid}, #{item.addr}, #{item.road}, #{item.onofflg}, #{item.ctlmode}, #{item.value}, #{item.time})" +
            "</foreach>" +
            "</script>"})
    void insertBatch(List<RelayActionDO> relays);
}
