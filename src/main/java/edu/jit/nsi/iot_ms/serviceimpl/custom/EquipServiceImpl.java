package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.EquipDO;
import edu.jit.nsi.iot_ms.mapper.EquipDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class EquipServiceImpl{
    @Autowired
    EquipDAO equipdao;
    @Autowired
    SensorCmdCfg sensorCmdCfg;
    @Autowired
    EnvThDefServiceImpl envCtlDefService;
//    @Autowired
//    InfluxdbDaoImpl influxdbDao;


    //用户新增设备
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public EquipDO addEquip(int cell_id, String equip_type, String equip_name, int termid, int addr, int road){
        List<EquipDO> equipDOList = equipdao.selectList(new EntityWrapper<EquipDO>().eq("termid", termid).eq("addr", addr).eq("road",road));
        if(equipDOList.size()!=0){
            return equipDOList.get(0);
        }
        EquipDO equip = new EquipDO(equip_name, equip_type, termid, addr, road, cell_id);
        envCtlDefService.addThCtl(cell_id, equip_type, -1,0);
        int ret = equipdao.insert(equip);
        if(ret < 0){
            return null;
        } else {
            return equip;
        }
    }

    //更新设备参数
    public boolean updateEquip(int id, int cell_id, String equip_name, int term_id, int addr, int road){
        EquipDO equipDO = equipdao.selectById(id);
        equipDO.setCellid(cell_id);
        equipDO.setDefname(equip_name);
        equipDO.setTermid(term_id);
        equipDO.setAddr(addr);
        equipDO.setRoad(road);
        int ret = equipdao.updateById(equipDO);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    //删除设备
    public boolean delEquip(int id){
        int ret = equipdao.deleteById(id);
        if(ret < 0){
            log.error("删除设备id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    //删除终端下的设备
    public boolean delEquipsInTerm(int tid){
        List<EquipDO> equplist= equipsInTerm(tid);
        for(EquipDO quip:equplist){
            if(!delEquip(quip.getId())){
                log.error("删除终端id{}对应的设备id{}失败!", tid, quip.getId());
                return false;
            }
        }
        return true;
    }

    //用户查询某生产单元下的设备信息
    public List<EquipDO> equip_list(int cell_id){
        return equipdao.selectList(new EntityWrapper<EquipDO>().eq("cellid", cell_id));
    }

    //用户查询某生产单元下的设备信息
    public List<String> ctlenv_list(int cell_id){
        List<String> envlist = new ArrayList<>();
        List<EquipDO> equiplist=  equipdao.selectList(new EntityWrapper<EquipDO>().eq("cellid", cell_id));
        for(EquipDO eq : equiplist){
            if(!envlist.contains(eq.getType())){
                envlist.add(eq.getType());
            }
        }
        return envlist;
    }

    //用户查询某生产单元下控制某类参数的的设备信息
    public List<EquipDO> cellParamEqp_list(int cell_id, String type){
        return equipdao.selectList(new EntityWrapper<EquipDO>().eq("cellid", cell_id).eq("type", type));
    }

    //用户根据equip_id查找信息
    public EquipDO equip_info(int equip_id){
        return equipdao.selectList(new EntityWrapper<EquipDO>().eq("id", equip_id)).get(0);
    }

    //查找终端下的所有设备
    public List<EquipDO> equipsInTerm(int term_id){
        return equipdao.selectList(new EntityWrapper<EquipDO>().eq("termid", term_id));
    }

    //用户根据equip_id查找信息
    public EquipDO equip_info2(int equip_id){
        List<EquipDO> equiplist = equipdao.selectList(new EntityWrapper<EquipDO>().eq("id", equip_id).last("LIMIT 1"));
        if(equiplist.size()==1){
            return equiplist.get(0);
        }else{
            return  null;
        }
    }

    //根据termid，485, road地址查询equipid信息
    public EquipDO getEquipbyTermAddr(int term_id, int addr, int road){
        List<EquipDO> equiplist = equipdao.selectList(new EntityWrapper<EquipDO>().eq("termid", term_id).eq("addr", addr).eq("road", road).last("LIMIT 1"));
        if(equiplist!=null && equiplist.size()==1){
            return equiplist.get(0);
        }else{
            return  null;
        }
    }
}
