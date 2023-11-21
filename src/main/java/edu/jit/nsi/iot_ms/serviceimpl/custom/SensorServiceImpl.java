package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.RegParam;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.CellDO;
import edu.jit.nsi.iot_ms.domain.SensorParamDO;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import edu.jit.nsi.iot_ms.domain.TermDO;
import edu.jit.nsi.iot_ms.mapper.CellDAO;
import edu.jit.nsi.iot_ms.mapper.SensorParamDAO;
import edu.jit.nsi.iot_ms.mapper.SensorPhyDAO;
import edu.jit.nsi.iot_ms.transport.CellParamData;
import edu.jit.nsi.iot_ms.transport.msg.TermInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @packageName: com.jit.iot.service.Impl
 * @className: SensorServiceImpl
 * @Description:
 * @author: xxz
 * @date: 2019/7/25 10:13
 */

@Slf4j
@Service
public class SensorServiceImpl {
    @Autowired
    SensorCmdCfg cmdCfg;
    @Resource
    SensorPhyDAO sensorPhyDAO;
    @Resource
    SensorParamDAO sensorParamDAO;
    @Resource
    CellDAO cellDAO;
    @Resource
    TerminalServiceImpl termService;
    @Resource
    SensorCmdCfg sensorCmdCfg;

    /**
     * 用户新增传感器
     * @param cell_id     生产单元ID
     * @param prod     产品名称
     * @param addr     配置的地址
     * @param sensor_name 用户自定义传感器名称
     */
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public SensorPhyDO addPhySensor(int cell_id, int term_id, String prod, int addr, String sensor_name){

        TermDO term = termService.getTermById(term_id);
        List<SensorPhyDO> sensorData = sensorPhyDAO.selectList(new EntityWrapper<SensorPhyDO>().eq("termid", term_id).eq("addr",addr).eq("product",prod));
        if(sensorData.size()!=0){
            return null;
        }
        SensorPhyDO sensorPhyDO = new SensorPhyDO(prod, term_id, addr, sensor_name, cell_id);
        int ret = sensorPhyDAO.insert(sensorPhyDO);
        if(ret < 0){
            return null;
        } else {
            int phyid = sensorPhyDO.getId();
            List<RegParam> paramList;
            if(term.getType()==1||term.getType()==5){
                paramList = sensorCmdCfg.getDefParms(prod);
            }else {
                paramList = sensorCmdCfg.getFixedParams(prod);
            }
            for(RegParam rp:paramList) {
                SensorParamDO paramDO = new SensorParamDO(rp.getReg(), rp.getEnvparam(), phyid);
                sensorParamDAO.insert(paramDO);
            }
            return sensorPhyDO;
        }
    }


    /**
     * 用户更新传感器配置
     * @param sid          传感器ID
     * @param cell_id     生产单元ID
     * @param addr        配置的地址
     * @param sensor_name 用户自定义传感器名称
     */
    public boolean updatePhySensor(int sid, int cell_id, int addr, String sensor_name){
        SensorPhyDO sensorPhyDO = sensorPhyDAO.selectById(sid);
        sensorPhyDO.setCellid(cell_id);
        sensorPhyDO.setAddr(addr);
        sensorPhyDO.setName(sensor_name);
        int ret = sensorPhyDAO.updateById(sensorPhyDO);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除物理传感器
     * @param sid       物理传感器ID
     * @return
     */
    public boolean deletePhySensor(int sid) {
        deleteSensorParam(sid);
        int ret = sensorPhyDAO.deleteById(sid);
        if(ret < 0){
            log.error("删除物理传感器{}失败!", sid);
            return false;
        } else {
            return true;
        }
    }


    /**
     * 删除传感器对应的采集参数
     * @param physid    物理传感器ID
     * @return
     */
    public boolean deleteSensorParam(int physid) {
        int ret = sensorParamDAO.delete(new EntityWrapper<SensorParamDO>().eq("phyid",physid));
        if(ret < 0){
            log.error("删除物理传感器{}对应的采集参数失败!", physid);
            return false;
        } else {
            return true;
        }
    }


    /**
     * 删除终端下的传感器
     * @param tid       终端ID
     * @return
     */
    public boolean deleteSensorsInTerm(int tid) {
        List<SensorPhyDO> sensorPhyDOList = termPhySensors(tid);
        for(SensorPhyDO phyDO : sensorPhyDOList) {
            if(!deletePhySensor(phyDO.getId())){
                log.error("删除终端id{}其中的物理传感器id{}失败，退出!", tid, phyDO.getId());
                return false;
            }
        }
        return true;
    }

    //查询phySensor下的paramSensor
    public List<SensorParamDO> sensorParams(int phyid){
        return sensorParamDAO.selectList(new EntityWrapper<SensorParamDO>().eq("phyid",phyid));
    }

    //查询生产单元下的物理传感器信息
    public List<SensorPhyDO> cellSensors(int cell_id)
    {
        return sensorPhyDAO.selectList(new EntityWrapper<SensorPhyDO>().eq("cellid",cell_id));
    }


    //查询生产单元下的传感器参数集合
    public List<SensorParamDO> cellParamsDO(int cell_id, boolean allenv){
        List<SensorParamDO> paramDOList = new ArrayList<>();
        List<SensorPhyDO> phsensors = cellSensors(cell_id);
        for(SensorPhyDO phy: phsensors){
            List<SensorParamDO> list = sensorParams(phy.getId());
            for(SensorParamDO snparam:list){
                if(!allenv && !sensorCmdCfg.isEnvToUsr(snparam.getParam()))
                    continue;
                else
                    paramDOList.add(snparam);
            }
        }
        if(paramDOList.size()>0)
            return paramDOList;
        else
            return null;
    }

    //查询生产单元下传感器参数的id、采集参数和传感器自定义名称
    public List<CellParamData> cellParamData(int cell_id, boolean allenv){
        List<CellParamData> paramDatas = new ArrayList<>();
        List<SensorPhyDO> phsensors = cellSensors(cell_id);
        for(SensorPhyDO phy: phsensors){
            //跳过继电器参数
            if(phy.getProduct().toLowerCase().contains("relay")||phy.getProduct().toLowerCase().contains("dma"))
                continue;
            List<SensorParamDO> paramDOS = sensorParams(phy.getId());
            for(SensorParamDO param:paramDOS){
                if(!allenv && !sensorCmdCfg.isEnvToUsr(param.getParam())) {
                    continue;
                }
                else {
                    String pa = param.getParam();
                    CellParamData cellParam = new CellParamData(param.getId(), pa, phy.getName());
                    cellParam.setSuffix(sensorCmdCfg.getEnvSuffix(pa));
                    cellParam.setTypeCHN(sensorCmdCfg.getEnvDesc(pa));
                    paramDatas.add(cellParam);
                }
            }
        }
        return paramDatas;
    }



    //查询终端下的物理传感器
    public List<SensorPhyDO> termPhySensors(int term_id)
    {
        return sensorPhyDAO.selectList(new EntityWrapper<SensorPhyDO>().eq("termid",term_id));
    }

    //查询终端下的传感器采集参数信息
    public List<SensorParamDO> termSensorParams(int term_id)
    {
        List<SensorPhyDO> phsensors = termPhySensors(term_id);
        List<Integer> coll = new ArrayList<>();
        for(SensorPhyDO phy: phsensors){
            coll.add(phy.getId());
        }
        return sensorParamDAO.selectList(new EntityWrapper<SensorParamDO>().in("phyid", coll));
    }

    //根据终端ids获取生产单元信息列表
    public List<TermInfo> getWSCellByTid(String termstr){
        List<TermInfo> termInfos = new ArrayList<>();
        String[] tids = termstr.split(",");
        for(String idstr: tids){
            int tid =Integer.parseInt(idstr);
            TermInfo tinfo = new TermInfo(tid);
            List<SensorPhyDO> sensorPhyDOList = sensorPhyDAO.selectList(new EntityWrapper<SensorPhyDO>().eq("termid",tid).last("limit 1"));
            if(sensorPhyDOList!=null && !sensorPhyDOList.isEmpty()){
                sensorPhyDOList.get(0).getCellid();
                CellDO celldo = cellDAO.selectById(sensorPhyDOList.get(0).getCellid());
                if(celldo!=null) {
                    tinfo.setAllInfo(celldo);
                }
            }
            termInfos.add(tinfo);
        }
        return termInfos;
    }

}
