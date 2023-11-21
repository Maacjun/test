package edu.jit.nsi.iot_ms.serviceimpl.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import edu.jit.nsi.iot_ms.config.RelayCtlCmd;
import edu.jit.nsi.iot_ms.config.SensorCmd;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.EnvirDataDO;
import edu.jit.nsi.iot_ms.domain.EquipDO;
import edu.jit.nsi.iot_ms.domain.RelayActionDO;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import edu.jit.nsi.iot_ms.mapper.EnvirDataDAO;
import edu.jit.nsi.iot_ms.mapper.RelayActionDAO;
import edu.jit.nsi.iot_ms.mapper.SensorPhyDAO;
import edu.jit.nsi.iot_ms.config.InfluxdbDaoImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.CellServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.ReportData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class IotServiceImpl {
    @Autowired
    SensorCmdCfg cmdCfg;
    @Autowired
    CellServiceImpl cellService;
    @Autowired
    SessionManager sessionManager;
    @Autowired
    EquipServiceImpl equipService;
    @Autowired
    SensorPhyDAO saddrdao;
    @Autowired
    EnvirDataDAO envdao;
    @Autowired
    InfluxdbDaoImpl influxDao;
    @Autowired
    RelayActionDAO relaydao;

    public void termSRList(int termid, List<SensorCmd> sensorlist, List<RelayCtlCmd> relaylist) {
        //找到所有gw_id对应的所有的cell_id
        //根据gw_id找到对应的所有sensor
        List<SensorPhyDO> saddrList = saddrdao.selectList(new EntityWrapper<SensorPhyDO>().eq("termid", termid));
        if ( saddrList!=null && saddrList.size() > 0) {
            Map<String, List> sensormap = new HashMap<String, List>();
            for (SensorPhyDO sensor : saddrList) {
                List<Integer> addr;
                String sensorType = sensor.getProduct();
                addr = sensormap.get(sensorType);
                if (null == addr) {
                    addr = new ArrayList<Integer>();
                    addr.add(sensor.getAddr());
                    sensormap.put(sensorType, addr);
                } else {
                    addr.add(sensor.getAddr());
                }
            }

            //遍历当前终端下的传感器类型添加Addr，并保存到list中
            for (String sensortype : sensormap.keySet()) {
                //生成传感器指令

                for (SensorCmd cmdmodel : cmdCfg.getSensorList()) {
                    if (cmdmodel.getType().equals(sensortype)) {
                        SensorCmd cmditem = new SensorCmd(cmdmodel);
                        cmditem.setAddr(sensormap.get(sensortype));
                        sensorlist.add(cmditem);
                    }
                }

                //生成继电器控制命令传给gw
                if (sensortype.toLowerCase().contains("relay")||sensortype.toLowerCase().contains("dma")) {
                    for(RelayCtlCmd ctlmodel:cmdCfg.getRelaylist()){
                        if(ctlmodel.getType().equals(sensortype)){
                            RelayCtlCmd ctlitem = new RelayCtlCmd(ctlmodel) ;
                            ctlitem.setAddr(sensormap.get(sensortype));
                            relaylist.add(ctlitem);
                        }
                    }
                }
            }
        }
    }

    /**
     * @Description 记录收到的周期上报的传感器数据，同时检查更新relayaction(人工控制继电器会有改变其状态)
     **/
    public void recordReport(int termid, List<ReportData> reports, int ctlmode){
        List<EnvirDataDO> envirslist = new ArrayList<EnvirDataDO>();
        List<RelayActionDO> relaysdb = new ArrayList<RelayActionDO>();
        Date now = new Date();
        String biotype = null;

        sessionManager.updateSessByReport(termid,  reports, ctlmode);

        //依次检查所有的继电器状态
        for(ReportData bean:reports) {
            int paramid = sessionManager.getParmid(termid, bean.getAddr(), bean.getReg());
            //记录传感器上报的数值
            envirslist.add(new EnvirDataDO(termid, paramid, bean.getAddr(), bean.getReg(), bean.getType(), bean.getValue(),now));
            //特别地，针对继电器记录其各路的更新情况
            if (bean.getType().toLowerCase().contains("relay") || bean.getType().toLowerCase().contains("dma")){
                relayStaChange(termid, bean.getAddr(),bean.getValue(), now, relaysdb, ctlmode);
            }
            if(biotype==null){
                biotype = sessionManager.getCellType(termid, bean.getAddr());
            }
        }
        if(biotype==null){
            biotype = "unkown";
        }

        //上报数据保存DB
        if(!envirslist.isEmpty()) {
            envdao.insertBatch(envirslist);
            influxDao.insertReport(biotype, envirslist);
        }

        //继电器有变化则更新DB
        if(!relaysdb.isEmpty()){
            relaydao.insertBatch(relaysdb);
        }
    }

    /**
     * @Description 检查继电器各路状态，当发送变化记录更新relayaction(人工控制继电器会有改变其状态)
     *
     **/
    private void relayStaChange(int termid, int addr, Float value, Date now, List<RelayActionDO> relaysdb, int ctlmode) {
        List<RelayActionDO> old_relaylst = relaydao.selectList(new EntityWrapper<RelayActionDO>().eq("termid", termid)
                .eq("addr", addr).orderBy("id", false).last("LIMIT 1"));

        int former = 0;
        if( old_relaylst != null && !old_relaylst.isEmpty()){
            former =  (int)old_relaylst.get(0).getValue();
        }

        int current = value.intValue();
        int mask = (int) ((former & 0xffff) ^ (current & 0xffff));   //按位与0xffff的目的是只取最低字节
        byte changed = 0, pos = 0, onoff = 0;
        while (mask > 0) {
            changed = (byte) (mask % 2);
            if (changed == 1) {
                onoff = (byte) (((current & 0xffff)>>pos) & 0x01);
                EquipDO equip = equipService.getEquipbyTermAddr(termid,addr,pos + 1);
                if(equip!=null){
//                    sessionManager.updateEquStaByCtrl(equip.getId(), onoff, now);
                    relaysdb.add(new RelayActionDO(equip.getId(), equip.getCellid(), termid, addr, (byte) (pos + 1), onoff, (byte) ctlmode, current, now));
                }
            }
            mask = (int) (mask >> 1);
            pos++;
        }
    }

}
