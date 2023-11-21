package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.EnvThrsdCtlDO;
import edu.jit.nsi.iot_ms.mapper.EnvThCtlDAO;
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
public class EnvThDefServiceImpl {
    @Autowired
    EnvThCtlDAO envthctldao;

    @Autowired
    SensorCmdCfg sensorCmdCfg;
    //用户新增设备
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public EnvThrsdCtlDO addThCtl(int cell_id, String ptype, int paramid,
//                             float wnup, float wndw,
//                             float actup,float actdw,
//                             int actupeqid, int actdweqid,
                                  int autofg){
        List<EnvThrsdCtlDO> envThrsdCtlDOS = envthctldao.selectList(new EntityWrapper<EnvThrsdCtlDO>().eq("cellid", cell_id).eq("param", ptype));
        if(envThrsdCtlDOS.size()!=0){
            return envThrsdCtlDOS.get(0);
        }
        EnvThrsdCtlDO ctlDO = new EnvThrsdCtlDO(cell_id, ptype,paramid,
                sensorCmdCfg.getEnvCtlWnUp(ptype),sensorCmdCfg.getEnvCtlWnDw(ptype),
                sensorCmdCfg.getEnvCtlActUp(ptype), sensorCmdCfg.getEnvCtlActDw(ptype),
                /*actupeqid, actdweqid,*/ autofg);
        int ret = envthctldao.insert(ctlDO);
        if(ret < 0){
            return null;
        } else {
            return ctlDO;
        }
    }

    //更新设备参数
    public boolean updateThCtl(int id, int paramid,
                               float wmup, float wmdw,
                               float actup, float actdw,
//                               int actupeqid, int actdweqid,
                               int autofg){
        EnvThrsdCtlDO ctlDO = envthctldao.selectById(id);
        ctlDO.setParamid(paramid);
        ctlDO.setWnup(wmup);
        ctlDO.setWndw(wmdw);
        ctlDO.setActup(actup);
        ctlDO.setActdw(actdw);
//        ctlDO.setActupeqid(actupeqid);
//        ctlDO.setActdweqid(actdweqid);
        ctlDO.setAutofg(autofg);
        int ret = envthctldao.updateById(ctlDO);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    //删除设备
    public boolean delThCtl(int id){
        int ret = envthctldao.deleteById(id);
        if(ret < 0){
            log.error("删除自动控制id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    //查询塘口下的所有控制参数
    public List<EnvThrsdCtlDO> lstThCtlParam(int cellid){
        return envthctldao.selectList(new EntityWrapper<EnvThrsdCtlDO>().eq("cellid", cellid));
    }
}
