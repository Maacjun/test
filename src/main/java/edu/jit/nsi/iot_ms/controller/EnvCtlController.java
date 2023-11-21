package edu.jit.nsi.iot_ms.controller;

import com.google.gson.JsonObject;
import edu.jit.nsi.iot_ms.Fegin.Authority.AuFeignClient;
import edu.jit.nsi.iot_ms.Fegin.SMS.SMSFeignClient;
import edu.jit.nsi.iot_ms.domain.*;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EnvCmpDefServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EnvThDefServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.custom.EnvTmDefServiceImpl;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.jit.nsi.iot_ms.commons.util.JacksonUtils.objectMapper;

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
@RequestMapping(value = "/envctldef")
@Api(description = "自动控制参数管理")
@ResponseResult
public class EnvCtlController {
    @Resource
    EnvThDefServiceImpl envThDefService;

    @Resource
    EnvTmDefServiceImpl envTmDefService;

    @Resource
    EnvCmpDefServiceImpl envCmpDefService;
//    @Qualifier("edu.jit.nsi.iot_ms.Fegin.SMS.SMSFeignClient")
    @Autowired
    SMSFeignClient smsFeignClient;
    @Autowired
    AuFeignClient auFeignClient;

    //查询单元下的阈值自动控制参数
    @ApiOperation(value = "查询单元下的阈值自动控制参数" ,  notes="查询单元下的阈值自动控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/lstThCtlParam", method = RequestMethod.GET)
    public List<EnvThrsdCtlDO> lstThCtlParam(@ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id) {
        return envThDefService.lstThCtlParam(cell_id);
    }


    //查询单元下的时间自动控制参数
    @ApiOperation(value = "查询单元下的时间自动控制参数" ,  notes="查询单元下的时间自动控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/lstTmCtlParam", method = RequestMethod.GET)
    public List<EnvTimeCtlDO> lstTmCtlParam(@ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id) {
        return envTmDefService.lstTmCtlParam(cell_id);
    }

    //查询单元下的比较自动控制参数
    @ApiOperation(value = "查询单元下的比较自动控制参数" ,  notes="查询单元下的比较自动控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/lstCmpCtlParam", method = RequestMethod.GET)
    public List<EnvCmpCtlDO> lstCmpCtlParam(@ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id) {
        return envCmpDefService.lstCmpCtlParam(cell_id);
    }

    //新增阈值自动环境控制
    @ApiOperation(value = "新增阈值自动环境控制" ,  notes="新增阈值自动环境控制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addThCtl", method = RequestMethod.POST)
    public EnvThrsdCtlDO addEnvCtl(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id,
            @ApiParam(name = "env_type", value = "被控环境参数") @RequestParam("env_type") String env_type,
            @ApiParam(name = "param_id", value = "参考环境参数id") @RequestParam("param_id") int param_id,
            @ApiParam(name = "auto_fg", value = "启用标识") @RequestParam("auto_fg") int auto_fg) throws Exception{
        return envThDefService.addThCtl(cell_id, env_type, param_id, auto_fg);
    }

    //新增时间自动环境控制
    @ApiOperation(value = "新增时间自动环境控制" ,  notes="新增时间自动环境控制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addTmCtl", method = RequestMethod.POST)
    public EnvTimeCtlDO addTmCtl(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id,
            @ApiParam(name = "env_type", value = "被控环境参数") @RequestParam("env_type") String env_type,
            @ApiParam(name = "time", value = "动作时间点") @RequestParam("time") String time,
            @ApiParam(name = "opt", value = "执行动作") @RequestParam("opt") int opt,
            @ApiParam(name = "auto_fg", value = "启用标识") @RequestParam("auto_fg") int auto_fg) throws Exception{
        return envTmDefService.addThCtl(cell_id, env_type, time,opt, auto_fg);
    }

    //新增比较自动环境控制
    @ApiOperation(value = "新增比较自动环境控制" ,  notes="新增比较自动环境控制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/addCmpCtl", method = RequestMethod.POST)
    public EnvCmpCtlDO addEnvCmpCtl(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam("cell_id") int cell_id,
            @ApiParam(name = "env_type", value = "被控环境参数") @RequestParam("env_type") String env_type,
            @ApiParam(name = "param1_id", value = "比较环境参数id1") @RequestParam("param1_id") int param1_id,
            @ApiParam(name = "param2_id", value = "比较环境参数id2") @RequestParam("param2_id") int param2_id,
            @ApiParam(name = "is_greater", value = "是否大于") @RequestParam("is_greater") boolean is_greater,
            @ApiParam(name = "distance", value = "数值差异") @RequestParam("distance") float distance,
            @ApiParam(name = "opt", value = "执行动作") @RequestParam("opt") int opt,
            @ApiParam(name = "auto_fg", value = "启用标识") @RequestParam("auto_fg") int auto_fg) throws Exception{
        return envCmpDefService.addCmpCtl(cell_id, env_type, param1_id, param2_id,
                                          is_greater, distance, opt, auto_fg);
    }

    //更新阈值自动环境控制参数
    @ApiOperation(value = "更新阈值自动环境控制参数" ,  notes="更新阈值自动环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateThCtl", method = RequestMethod.POST)
    public boolean updateThCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id,
            @ApiParam(name = "env_type", value = "被控环境参数") @RequestParam("env_type") String env_type,
            @ApiParam(name = "param_id", value = "参考环境参数id") @RequestParam("param_id") int param_id,
            @ApiParam(name = "wnup", value = "告警上限", required = false) @RequestParam(value="wnup", required = false) float wnup,
            @ApiParam(name = "wndw", value = "告警下限", required = false) @RequestParam(value="wndw", required = false) float wndw,
            @ApiParam(name = "actup", value = "操作上限", required = false) @RequestParam(value="actup", required = false) float actup,
            @ApiParam(name = "actdw", value = "操作下限", required = false) @RequestParam(value="actdw", required = false) float actdw,
            @ApiParam(name = "autofg", value = "启用标识", required = false) @RequestParam(value="autofg", required = false) int autofg) throws Exception{
        return envThDefService.updateThCtl(id,  param_id, wnup, wndw, actup,actdw, autofg);
    }

    //更新时间自动环境控制参数
    @ApiOperation(value = "更新时间自动环境控制参数" ,  notes="更新时间自动环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateTmCtl", method = RequestMethod.POST)
    public boolean updateTmCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id,
            @ApiParam(name = "env_type", value = "被控环境参数") @RequestParam("env_type") String env_type,
            @ApiParam(name = "time", value = "动作时间点") @RequestParam("time") String time,
            @ApiParam(name = "opt", value = "执行动作") @RequestParam("opt") int opt,
            @ApiParam(name = "autofg", value = "启用标识", required = false) @RequestParam(value="autofg", required = false) int autofg) throws Exception{
        return envTmDefService.updateTmCtl(id,  env_type, time, opt, autofg);
    }

    //更新阈值自动环境控制参数
    @ApiOperation(value = "更新比较自动环境控制参数" ,  notes="更新比较自动环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/updateCmpCtl", method = RequestMethod.POST)
    public boolean updateCmpCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id,
            @ApiParam(name = "param1_id", value = "比较环境参数id1") @RequestParam("param1_id") int param1_id,
            @ApiParam(name = "param2_id", value = "比较环境参数id2") @RequestParam("param2_id") int param2_id,
            @ApiParam(name = "is_greater", value = "是否大于") @RequestParam("is_greater") boolean is_greater,
            @ApiParam(name = "distance", value = "数值差异") @RequestParam("distance") float distance,
            @ApiParam(name = "opt", value = "执行动作") @RequestParam("opt") int opt,
            @ApiParam(name = "auto_fg", value = "启用标识") @RequestParam("auto_fg") int auto_fg) throws Exception{
        return envCmpDefService.updateCmpCtl(id, param1_id, param2_id, is_greater, distance,opt,auto_fg);
    }

    //删除阈值环境控制参数
    @ApiOperation(value = "删除阈值环境控制参数" ,  notes="删除阈值环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/delThCtl", method = RequestMethod.DELETE)
    public boolean delThCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id)throws Exception{
        return envThDefService.delThCtl(id);
    }

    //删除时间环境控制参数
    @ApiOperation(value = "删除时间环境控制参数" ,  notes="删除时间环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/delTmCtl", method = RequestMethod.DELETE)
    public boolean delTmCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id)throws Exception{
        return envTmDefService.delTmCtl(id);
    }

    //删除比较环境控制参数
    @ApiOperation(value = "删除比较环境控制参数" ,  notes="删除比较环境控制参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/delCmpCtl", method = RequestMethod.DELETE)
    public boolean delCmpCtl(
            @ApiParam(name = "id", value = "序列ID") @RequestParam("id") int id)throws Exception{
        return envCmpDefService.delCmpCtl(id);
    }

    //进行短信测试
    @ApiOperation(value = "短信服务测试，用于测试，查找用户以及发短信给指定号码",notes = "短信服务测试，用于测试，查找用户以及发短信给指定号码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @PostMapping("/smsTest")
    public boolean smsTest(@ApiParam(name = "用户名", value = "用户名") @RequestParam(value = "userName", required = false) String userName,
                           @ApiParam(name = "手机号码", value = "手机号码") @RequestParam(value = "tel", required = false) String tel, HttpServletRequest request) {
        log.info("进入短信测试服务");
        authResponse loginfo = auFeignClient.logIn("SMS", "SMS");
        Object data = loginfo.getData();
            LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) data;
            // 获取 "token" 字段的值
            Object tokenObj = dataMap.get("token");
            String token = (String) tokenObj;
            // 现在你可以安全地使用 token 字符串
            log.info("Token: {}", token);
        authResponse userinfo=(auFeignClient.userInfoByName("ss",token));
        Object detail = userinfo.getData();
        log.info("查找的用户信息{}", userinfo);
        log.info("detail{}",detail);
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) detail;
        // 获取 "token" 字段的值
        Object tel_ = map.get("tel");
        String userTel = (String) tel_;
        log.info("tel{}",userTel);
        smsFeignClient.warnMessage(1,"塘口","do",1.2f,"1","17714429653");
        return true;

    }
}
