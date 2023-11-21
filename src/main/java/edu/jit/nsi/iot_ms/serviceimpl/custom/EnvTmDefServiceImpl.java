package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.EnvTimeCtlDO;
import edu.jit.nsi.iot_ms.mapper.EnvTmCtlDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @packageName: com.jit.iot.service.Impl
 * @className: EquipServiceImpl
 * @Description:
 * @author: xxz
 * @date: 2019/7/30 19:27
 */

@Slf4j
@Service
public class EnvTmDefServiceImpl {
    @Autowired
    EnvTmCtlDAO envtmctldao;

    @Autowired
    SensorCmdCfg sensorCmdCfg;
    //用户新增设备
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public EnvTimeCtlDO addThCtl(int cell_id, String ptype,  String time, int opt, int autofg){
        EnvTimeCtlDO tmCtl = new EnvTimeCtlDO(cell_id, ptype, time, opt, autofg);
        int ret = envtmctldao.insert(tmCtl);
        if(ret < 0){
            return null;
        } else {
            return tmCtl;
        }
    }

    //更新设备参数
    public boolean updateTmCtl(int id, String param,String time, int opt, int autofg){
        EnvTimeCtlDO tmCtl = envtmctldao.selectById(id);
        tmCtl.setParam(param);
        tmCtl.setTime(time);
        tmCtl.setOpt(opt);
        tmCtl.setAutofg(autofg);
        int ret = envtmctldao.updateById(tmCtl);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    //删除设备
    public boolean delTmCtl(int id){
        int ret = envtmctldao.deleteById(id);
        if(ret < 0){
            log.error("删除自动控制id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    //查询塘口下的所有控制参数
    public List<EnvTimeCtlDO> lstTmCtlParam(int cellid){
        return envtmctldao.selectList(new EntityWrapper<EnvTimeCtlDO>().eq("cellid", cellid));
    }
}
