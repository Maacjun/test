package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.domain.EnvCmpCtlDO;
import edu.jit.nsi.iot_ms.mapper.EnvCmpCtlDAO;
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
public class EnvCmpDefServiceImpl {
    @Autowired
    EnvCmpCtlDAO envCmpctldao;

    //用户新增设备控制
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public EnvCmpCtlDO addCmpCtl(int cell_id, String ptype, int paramid1,int paramid2,
                                  boolean grt, float dis, int op, int autofg){
        List<EnvCmpCtlDO> envcmdCtlDOS = envCmpctldao.selectList(new EntityWrapper<EnvCmpCtlDO>().eq("cellid", cell_id).eq("param", ptype).eq("grthn", grt));
        if(envcmdCtlDOS.size()!=0){
            return envcmdCtlDOS.get(0);
        }
        EnvCmpCtlDO ctlDO = new EnvCmpCtlDO(cell_id, ptype,paramid1,paramid2, grt, dis, op, autofg);
        int ret = envCmpctldao.insert(ctlDO);
        if(ret < 0){
            return null;
        } else {
            return ctlDO;
        }
    }

    //更新设备参数
    public boolean updateCmpCtl(int id, int paramid1,int paramid2,
                                boolean grt, float dis, int op, int autofg){
        EnvCmpCtlDO ctlDO = envCmpctldao.selectById(id);
        ctlDO.setParamid1(paramid1);
        ctlDO.setParamid2(paramid2);
        ctlDO.setGrthn(grt);
        ctlDO.setDist(dis);
        ctlDO.setOpt(op);
        ctlDO.setAutofg(autofg);
        int ret = envCmpctldao.updateById(ctlDO);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    //删除设备
    public boolean delCmpCtl(int id){
        int ret = envCmpctldao.deleteById(id);
        if(ret < 0){
            log.error("删除自动控制id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    //查询塘口下的所有控制参数
    public List<EnvCmpCtlDO> lstCmpCtlParam(int cellid){
        return envCmpctldao.selectList(new EntityWrapper<EnvCmpCtlDO>().eq("cellid", cellid));
    }
}
