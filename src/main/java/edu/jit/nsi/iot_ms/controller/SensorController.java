package edu.jit.nsi.iot_ms.controller;


import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.SensorPhyDO;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.custom.SensorServiceImpl;
import edu.jit.nsi.iot_ms.transport.CellParamData;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @packageName: com.jit.iot.jit.edu.nsi.controller
 * @className: SensorCroller
 * @Description:
 * @author: xxz
 * @date: 2019/7/25 10:05
 */

//@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/sensordef")
@Api(description = "传感器配置接口")
@ResponseResult
public class SensorController {
    @Autowired
    SensorCmdCfg cmdCfg;

    @Autowired
    SensorServiceImpl sensorService;


    //用户查看传感器类型（固定和自定义终端）
    @ApiOperation(value = "根据终端厂商查询其产品列表" ,  notes="列出终端类型(manu/termtype)对应的传感器种类(product)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listProduct", method = RequestMethod.GET)
    //用户查看可以添加的传感器类型
    public Set<String> listProduct(
            @ApiParam(name = "ttid", value = "终端类型（整数值123）") @RequestParam("ttid") int ttid) {
        Set<String> typelist = null;
        typelist = cmdCfg.getProductsbyID(ttid);
        return typelist;
    }


    //生产单元新增传感器
    @ApiOperation(value = "新增传感器" ,  notes="在终端下新增传感器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addSensor", method = RequestMethod.POST)
    public SensorPhyDO addSensor(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id,
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam("term_id") int term_id,
            @ApiParam(name = "addr", value = "传感器地址标识, 根据termtype, 1:485地址 其他:地址为1") @RequestParam("addr") int addr,
            @ApiParam(name = "product", value = "产品名称，从Term接口查询终端厂商及产品product") @RequestParam("product") String product,
            @ApiParam(name = "sensor_name", value = "自定义传感器名称 例如:东南角") @RequestParam("sensor_name") String sensor_name) throws Exception{
        return sensorService.addPhySensor(cell_id, term_id, product, addr,  sensor_name);
    }


    //用户查询生产单元下的传感器信息
    @ApiOperation(value = "查询生产单元下所有传感器" ,  notes="列出生产单元下的所有传感器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/cellSensors", method = RequestMethod.GET)
    public List<SensorPhyDO> cellSensors(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") int cell_id) {
        return sensorService.cellSensors(cell_id);
    }


    //查询生产单元下的环境采集参数，用于初始化数据展示界面
    @ApiOperation(value = "查询生产单元下的环境采集参数" ,  notes="查询生产单元下的环境采集参数，用于初始化数据展示界面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/cellParams", method = RequestMethod.GET)
    public List<CellParamData> cellParams(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") int cell_id,
            @ApiParam(name = "all_env", value = "是否呈现养殖户") @RequestParam(value="all_env",defaultValue="true", required = false) boolean all_env) {
        return  sensorService.cellParamData(cell_id,all_env);
    }


    //用户查询传感器信息
    @ApiOperation(value = "查询终端下的所有传感器" ,  notes="列出终端下的所有传感器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termSensors", method = RequestMethod.GET)
    public List<SensorPhyDO> termSensors(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") int term_id) {
        return sensorService.termPhySensors(term_id);
    }

    //更改传感器
    @ApiOperation(value = "更新传感器逻辑配置" ,  notes="更新传感器逻辑配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateSensor", method = RequestMethod.POST)
    public boolean updateSensor(
            @ApiParam(name = "id", value = "传感器ID") @RequestParam("id") int id,
            @ApiParam(name = "cell_id", value = "生产单元ID", required = false) @RequestParam(value="cell_id", required = false) int cell_id,
            @ApiParam(name = "addr", value = "传感器地址标识, 根据termtype, 1:485地址 其他:地址为1", required = false) @RequestParam(value="addr", required = false) int addr,
            @ApiParam(name = "sensor_name", value = "自定义传感器名称 例如:东南角", required = false) @RequestParam(value="sensor_name", required = false) String sensor_name) throws Exception{
        return sensorService.updatePhySensor(id, cell_id, addr, sensor_name);
    }

    //删除传感器
    @ApiOperation(value = "删除传感器" ,  notes="删除传感器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/deleteSensor", method = RequestMethod.DELETE)
    public boolean deleteSensor(
            @ApiParam(name = "sid", value = "传感器ID") @RequestParam("sid") int sid) {
        return sensorService.deletePhySensor(sid);
    }

}
