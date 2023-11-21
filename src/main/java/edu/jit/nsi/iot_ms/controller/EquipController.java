package edu.jit.nsi.iot_ms.controller;


import edu.jit.nsi.iot_ms.config.EquipType;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.EquipDO;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EquipServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @packageName: com.jit.iot.jit.edu.nsi.controller
 * @className: EquipController
 * @Description:
 * @author: xxz
 * @date: 2019/7/30 19:27
 */

@Slf4j
//@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/equipdef")
@Api(description = "设备配置管理")
@ResponseResult
public class EquipController {
    @Autowired
    SensorCmdCfg cmdCfg;
    @Resource
    EquipServiceImpl equipService;

    //用户查看可以添加的设备类型
    @ApiOperation(value = "查询平台下的设备类型" ,  notes="列出支持的设备类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/showEquipType", method = RequestMethod.GET)
    public List<EquipType> showEquipType() {
        return cmdCfg.getEquiplist();
    }


    //查询终端下控制的设备
    @ApiOperation(value = "查询终端下控制的设备" ,  notes="查询某个终端下接入的被控设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listTermEquip", method = RequestMethod.GET)
    public List<EquipDO> listTermEquip(@ApiParam(name = "term_id", value = "终端ID") @RequestParam("term_id") int term_id) {
        return equipService.equipsInTerm(term_id);
    }

    //查询生产单元下的设备
    @ApiOperation(value = "查询生产单元下的设备" ,  notes="列出生产单元下的所有控制设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listEquips", method = RequestMethod.GET)
    public List<EquipDO> listEquips(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id) {
        return equipService.equip_list(cell_id);
    }

    //查询生产单元下设备控制的参数类型
    @ApiOperation(value = "查询生产单元下设备控制的参数类型" ,  notes="查询生产单元下设备控制的参数类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listCtrlEnrv", method = RequestMethod.GET)
    public List<String> listCtrlEnrv(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id) {
        return equipService.ctlenv_list(cell_id);
    }

    //用户新增设备
    @ApiOperation(value = "新增设备" ,  notes="在生产单元下新增设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addEquip", method = RequestMethod.POST)
    public EquipDO addEquip(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id,
            @ApiParam(name = "equip_type", value = "被控设备类型") @RequestParam("equip_type") String equip_type,
            @ApiParam(name = "equip_name", value = "自定义设备名称") @RequestParam("equip_name") String equip_name,
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam("term_id") int term_id,
            @ApiParam(name = "addr", value = "485地址") @RequestParam("addr") int addr,
            @ApiParam(name = "road", value = "继电器第几路") @RequestParam("road") int road) throws Exception{
        return equipService.addEquip(cell_id, equip_type, equip_name, term_id, addr, road);
    }

    //更新设备配置参数
    @ApiOperation(value = "更新设备逻辑配置" ,  notes="更新设备逻辑配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateEquip", method = RequestMethod.POST)
    public boolean updateEquip(
            @ApiParam(name = "id", value = "设备ID") @RequestParam("id") int id,
            @ApiParam(name = "cell_id", value = "生产单元ID", required = false) @RequestParam(value="cell_id", required = false) int cell_id,
            @ApiParam(name = "equip_name", value = "自定义设备名称", required = false) @RequestParam(value="equip_name", required = false) String equip_name,
            @ApiParam(name = "term_id", value = "终端ID", required = false) @RequestParam(value="term_id", required = false) int term_id,
            @ApiParam(name = "addr", value = "485地址", required = false) @RequestParam(value="addr", required = false) int addr,
            @ApiParam(name = "road", value = "继电器第几路", required = false) @RequestParam(value="road", required = false) int road) throws Exception{
        return equipService.updateEquip(id, cell_id, equip_name, term_id, addr, road);
    }

    //删除设备
    @ApiOperation(value = "删除被控设备" ,  notes="删除被控设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/delEquip", method = RequestMethod.DELETE)
    public boolean delEquip(
            @ApiParam(name = "id", value = "设备ID") @RequestParam("id") int id)throws Exception{
        return equipService.delEquip(id);
    }
}
