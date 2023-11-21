package edu.jit.nsi.iot_ms.controller;

import edu.jit.nsi.iot_ms.commons.pages.PageQO;
import edu.jit.nsi.iot_ms.commons.pages.PageVO;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.config.TermType;
import edu.jit.nsi.iot_ms.domain.TermDO;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.custom.TerminalServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @packageName: com.jit.iot.jit.edu.nsi.controller
 * @className: TermController
 * @author: xxz
 * @date: 2019/7/25 10:05
 */

@Slf4j
//@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/termdef")
@Api(description = "终端配置")
@ResponseResult
public class TermController {
    @Resource
    TerminalServiceImpl termService;
    @Autowired
    SensorCmdCfg cmdCfg;

    //查询终端类型
    @ApiOperation(value = "查询终端厂商及产品" ,  notes="列出系统内所有终端的生产厂家")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listTermManus", method = RequestMethod.GET)
    public List<TermType> listTermManus() {
        return cmdCfg.getManus();
    }


//    //查询终端类型
//    @ApiOperation(value = "查询厂商的产品" ,  notes="列出终端厂家的所有终端产品")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
//    })
//    @RequestMapping(value = "/listManuProducts", method = RequestMethod.GET)
//    public List<String> listManuProducts(
//            @ApiParam(name = "manu", value = "终端厂家") @RequestParam(value = "manu") String manu) {
//        return cmdCfg.getManuProducts(manu);
//    }

    //用户新增终端
    @ApiOperation(value = "新增终端" ,  notes="新增终端")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addTerm", method = RequestMethod.POST)
    public TermDO addTerm(
            @ApiParam(name = "type", value = "终端类型（整型）") @RequestParam("type") int type,
            @ApiParam(name = "deveui", value = "终端串号（十六个16进制）", required = false)  @RequestParam(value="deveui", required = false) String deveui,
            @ApiParam(name = "manu", value = "终端厂商") @RequestParam("manu") String manu,
            @ApiParam(name = "product", value = "终端产品") @RequestParam("product") String product,
            @ApiParam(name = "user", value = "归属用户名") @RequestParam("user") String user,
            @ApiParam(name = "name", value = "用户自定义终端名") @RequestParam("name") String name,
            @ApiParam(name = "datacycle", value = "终端数据上报周期") @RequestParam(value="datacycle",defaultValue="5") int datacycle,
            @ApiParam(name = "preheat", value = "传感器预热时长") @RequestParam(value="preheat", required = false) int preheat,
            @ApiParam(name = "toplat", value = "上报其他平台") @RequestParam(value="toplat", required = false) int toplat)
            throws Exception{
        return termService.addTerm(type, deveui, name, user, manu, product, datacycle, preheat,toplat);
    }


    //更改终端逻辑配置
    @ApiOperation(value = "更新终端逻辑配置" ,  notes="更新终端逻辑配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateTerm", method = RequestMethod.POST)
    public boolean updateTerm(
            @ApiParam(name = "id", value = "终端ID") @RequestParam("id") int id,
            @ApiParam(name = "deveui", value = "终端串号（十六个16进制）", required = false) @RequestParam(value="deveui", required = false) String deveui,
            @ApiParam(name = "user", value = "归属用户名", required = false) @RequestParam(value="user", required = false) String user,
            @ApiParam(name = "name", value = "用户自定义终端名", required = false)  @RequestParam(value="name", required = false) String name,
            @ApiParam(name = "datacycle", value = "终端数据上报周期") @RequestParam("datacycle") int datacycle,
            @ApiParam(name = "preheat", value = "传感器预热时长") @RequestParam(value="preheat", required = false) int preheat,
            @ApiParam(name = "toplat", value = "上报其他平台") @RequestParam(value="toplat", required = false) int toplat)
            throws Exception{
        return termService.updateTerm(id, deveui, user, name,datacycle,preheat,toplat);
    }

    //删除终端
    @ApiOperation(value = "删除终端" ,  notes="删除指定终端")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/deleteTerm", method = RequestMethod.DELETE)
    public boolean deleteTerm(
            @ApiParam(name = "id", value = "终端ID") @RequestParam("id") int id) {
        return termService.deleteTerm(id);
    }


    //列出所有终端及登录状态
    @ApiOperation(value = "查询系统内的所有终端" ,  notes="列出系统内所有终端")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listAllTerms", method = RequestMethod.GET)
    public PageVO<TermDO> listAllTerms(PageQO pageQO) {
        return termService.pglistAllTerms(pageQO);
    }


    //列出某个用户所有终端及登录状态
    @ApiOperation(value = "查询用户的终端" ,  notes="列出用户的所有终端")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/listTermsByUsr", method = RequestMethod.GET)
    public PageVO<TermDO> listTermsByUsr(
            @ApiParam(name = "user_name", value = "用户名")@RequestParam("user_name") String user_name,
            PageQO pageQO) {
        return termService.pglistTermsByUsr(pageQO,user_name);
    }

    //更新终端参数

}
