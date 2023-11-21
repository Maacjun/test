package edu.jit.nsi.iot_ms.controller;

import edu.jit.nsi.iot_ms.commons.pages.PageQO;
import edu.jit.nsi.iot_ms.commons.pages.PageVO;
import edu.jit.nsi.iot_ms.config.AgriCellType;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.CellDO;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.custom.CellServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.SensorServiceImpl;
import edu.jit.nsi.iot_ms.transport.msg.TermInfo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @packageName: com.jit.iot.jit.edu.nsi.controller
 * @className: CellService
 * @author: xxz
 * @date: 2019/7/25 10:05
 */

//@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/celldef")
@Api(description = "生产单元配置接口")
@ResponseResult
public class CellController {
    @Resource
    CellServiceImpl cellService;
    @Resource
    SensorServiceImpl sensorService;
    @Autowired
    SensorCmdCfg cmdCfg;

    @ApiOperation(value = "查询智慧农业生产单元类型" ,  notes="水产/种植/养殖/大田")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listCellTypes", method = RequestMethod.GET)
    public List<AgriCellType> listCellTypes() {
        return cmdCfg.getCellType();
    }

    @ApiOperation(value = "查询农产品" ,  notes="种植养殖品种")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listAgriProducts", method = RequestMethod.GET)
    public List<String> listAgriProducts(
            @ApiParam(name = "celltype", value = "生产单元类型") @RequestParam("celltype") String celltype) {
        return cmdCfg.getAgriProducts(celltype);
    }

    //用户新增生产单元
    @ApiOperation(value = "新增生产单元" ,  notes="用户新增生产单元")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addCell", method = RequestMethod.POST)
    public CellDO addCell(@ApiParam(name = "length", value = "长度") @RequestParam("length") float length,
                           @ApiParam(name = "width", value = "宽度") @RequestParam("width") float width,
                           @ApiParam(name = "longitude", value = "经度")@RequestParam("longitude") double longitude,
                           @ApiParam(name = "latitude", value = "纬度")@RequestParam("latitude") double latitude,
                           @ApiParam(name = "cell_type", value = "生产单元的类型(从查询农业生产单元类型获取)")@RequestParam("cell_type") String cell_type,
                           @ApiParam(name = "agri_prod", value = "生产单元下的农产品（从查询该生产单元下的农产品")@RequestParam("agri_prod") String agri_prod,
                           @ApiParam(name = "cell_name", value = "用户自己定义生产单元的名字，例如：张三河蟹1号塘口")@RequestParam("cell_name") String cell_name,
                           @ApiParam(name = "user_name", value = "归属的用户名")@RequestParam("user_name") String user_name
                           ) throws Exception{
        //调用service
        return cellService.add_cell(length, width, longitude, latitude, cell_type, agri_prod, cell_name, user_name);
    }

    //查询用户生产单元信息
    @ApiOperation(value = "查询用户的所有生产单元" ,  notes="列出用户的所有生产单元")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })


    @RequestMapping(value = "/listUserCells", method = RequestMethod.GET)
    public PageVO<CellDO> listUserCells(
            @ApiParam(name = "username", value = "用户名") @RequestParam("username") String username,
                                        PageQO pageQO) {
        return cellService.getUsrCells(pageQO, username);
    }


    //根据单元id获取生产单元信息
    @ApiOperation(value = "根据单元id获取生产单元信息" ,  notes="列出单元id对应的生产单元信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listCellsByIds", method = RequestMethod.GET)
    public List<CellDO> listCellsByIds(
            @ApiParam(name = "ids", value = "cell id列表,逗号分隔") @RequestParam("ids") String ids) {
        return cellService.getCellByIds(ids);
    }

    //根据term终端id获取生产单元信息(气象站)
    @ApiOperation(value = "根据气象站term终端id获取生产单元信息" ,  notes="根据气象站term终端id获取生产单元信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listCellsByTermIds", method = RequestMethod.GET)
    public List<TermInfo> listCellsByTermIds(
            @ApiParam(name = "termids", value = "term id列表,逗号分隔") @RequestParam("termids") String termids) {
        return sensorService.getWSCellByTid(termids);
    }

    //更新生产单元
    @ApiOperation(value = "更新生产单元" ,  notes="用户更新生产单元")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateCell", method = RequestMethod.POST)
    public boolean updateCell(
            @ApiParam(name = "id", value = "生产单元ID") @RequestParam("id") int id,
            @ApiParam(name = "length", value = "长度", required = false) @RequestParam(value="length", required = false) float length,
            @ApiParam(name = "width", value = "宽度", required = false) @RequestParam(value="width", required = false) float width,
            @ApiParam(name = "longitude", value = "经度", required = false) @RequestParam(value="longitude", required = false) double longitude,
            @ApiParam(name = "latitude", value = "纬度", required = false) @RequestParam(value="latitude", required = false) double latitude,
            @ApiParam(name = "cell_type", value = "生产单元的类型", required = false) @RequestParam(value="cell_type", required = false) String cell_type,
            @ApiParam(name = "agri_prod", value = "生产单元下的农产品", required = false) @RequestParam(value="agri_prod", required = false) String agri_prod,
            @ApiParam(name = "cell_name", value = "自定义名字", required = false) @RequestParam(value="cell_name", required = false) String cell_name,
            @ApiParam(name = "user_name", value = "用户名", required = false) @RequestParam(value="user_name", required = false) String user_name
    ) throws Exception{
        //调用service
        return cellService.update_cell(id, length, width, longitude, latitude, cell_type, agri_prod, cell_name, user_name);
    }

    //删除生产单元
    @ApiOperation(value = "删除具体的生产单元" ,  notes="删除ID对应的生产单元")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/delCell", method = RequestMethod.DELETE)
    public boolean delCell(
            @ApiParam(name = "id", value = "生产单元ID") @RequestParam("id") int id) {
        return cellService.delete_cell(id);
    }


}
