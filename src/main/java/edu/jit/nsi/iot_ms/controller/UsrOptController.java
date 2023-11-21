package edu.jit.nsi.iot_ms.controller;


import edu.jit.nsi.iot_ms.domain.AutoActionDO;
import edu.jit.nsi.iot_ms.domain.WarnDO;
import edu.jit.nsi.iot_ms.transport.msg.*;
import edu.jit.nsi.iot_ms.responseResult.result.ResponseResult;
import edu.jit.nsi.iot_ms.serviceimpl.impl.AppServiceImpl;
import edu.jit.nsi.iot_ms.serviceimpl.impl.EvnCtlServiceImpl;
import edu.jit.nsi.iot_ms.session.SessionManager;
import edu.jit.nsi.iot_ms.transport.*;
import edu.jit.nsi.iot_ms.transport.tcp.EquipNewRsp;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
//@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@Api(description = "用户操作接口")
@RequestMapping(value = "opt")
@ResponseResult
public class UsrOptController {
    @Autowired
    AppServiceImpl appService;

    @Autowired
    EvnCtlServiceImpl envCtlService;
    private final Map<Integer,DeferredResult<CtlRspMsg>> responseMap =new HashMap<Integer,DeferredResult<CtlRspMsg>>();

    /**
     * 查看某生产单元实时数据
     * @param cell_id 生产单元ID
     * @return
     */
    @ApiOperation(value = "查询实时数据" ,  notes="查询生产单元当前的传感器数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/rtQuery", method = RequestMethod.GET)
    public List<EnviNewSmryRsp> rtQuery(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") Integer cell_id,
    @ApiParam(name = "all_env", value = "是否呈现养殖户") @RequestParam(value="all_env",defaultValue="true", required = false) boolean all_env) {
        return  appService.onCellEnvSmryQuery(cell_id,all_env);
    }

    /**
     * 查看某生产单元设备状态
     * @param cell_id 生产单元ID
     * @return
     */
    @ApiOperation(value = "查询设备状态" ,  notes="查询生产单元设备当前的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/equQuery", method = RequestMethod.GET)
    public List<EquipNewRsp> equQuery(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") Integer cell_id) {
        return  appService.onCellEqpQuery(cell_id);
    }

    /**
     * 查看某用户下所有的终端状态
     * @param usrname 用户名
     * @return
     */
    @ApiOperation(value = "查询用户下面所有终端的状态" ,  notes="查询用户下面所有终端的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/usrTermSta", method = RequestMethod.GET)
    public List<SessionManager.TermSta> usrTermSta(
            @ApiParam(name = "usrname", value = "用户名") @RequestParam(value = "usrname") String usrname) {
        return  appService.getUsrTermSta(usrname);
    }

    /**
     * 查看系统下所有的终端状态
     * @return
     */
    @ApiOperation(value = "查询系统下所有终端的状态" ,  notes="查询系统下所有终端的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/allTermSta", method = RequestMethod.GET)
    public List<SessionManager.TermSta> allTermSta() {
        return  appService.getAllTermSta();
    }

    /**
     * 查看系统下当前终端的状态
     * @return
     */
    @ApiOperation(value = "查询系统下当前终端的状态" ,  notes="查询系统下当前终端的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termSta", method = RequestMethod.GET)
    public TermStatus termSta( @ApiParam(name = "termid", value = "用户名") @RequestParam(value = "termid") Integer termid) {
        return  appService.getNewTermSta(termid);
    }


    /**
     * 查询一段时间内终端的状态
     * @return
     */
    @ApiOperation(value = "查询一段时间内终端的状态" ,  notes="查询一段时间内终端的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/timeTermSta", method = RequestMethod.GET)
    public List<DailyTermStatus> timeTermSta(@ApiParam(name = "termid", value = "用户名") @RequestParam(value = "termid") Integer termid,
                                             @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
                                             @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.getDailyTermSta(termid, start_time, end_time);
    }


    /**
     * 查询一段时间内所有气象终端状态
     * @return
     */
    @ApiOperation(value = "查询一段时间内所有气象终端状态" ,  notes="查询一段时间内所有气象终端状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/hisStaByTerm", method = RequestMethod.GET)
    public List<HisTermStaAggr> hisStaByTerm(@ApiParam(name = "term_lst", value = "终端列表") @RequestParam(value = "term_lst") String term_lst,
                                             @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
                                             @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.getHisTermStaAggr(term_lst, start_time, end_time);
    }


    /**
     * 查询一段时间内（按天）所有气象参数统计
     * @return
     */
    @ApiOperation(value = "查询一段时间内（按天）所有气象参数统计" ,  notes="查询一段时间内（按天）所有气象参数统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/dailyParamByTerm", method = RequestMethod.GET)
    public List<WhrStnAnaly> dailyParamByTerm(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "type", value = "气象参数") @RequestParam(value = "type") String type,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.getDailyParamAggr(term_id, type, start_time, end_time);
    }


    /**
     * 查询某气象站日出日落和时长
     * @return
     */
    @ApiOperation(value = "查询某气象站日出日落和时长" ,  notes="查询某气象站日出日落和时长")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termSunTime", method = RequestMethod.GET)
    public List<WhrStnSunRiseSetAnaly> termSunTime(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.analySunTimeFromSeq(term_id,  start_time, end_time);
    }


    /**
     * 查询某气象站日照积分
     * @return
     */
    @ApiOperation(value = "查询某气象站日照积分" ,  notes="查询某气象站日照积分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termIlluIntgl", method = RequestMethod.GET)
    public List<WhrStnIlluIntgl> termIlluIntgl(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.integalIllu(term_id,  start_time, end_time);
    }

    /**
     * 查询当前终端的在线、离线数量及列表
     * @return
     */
    @ApiOperation(value = "查询当前终端的在线、离线数量及列表" ,  notes="查询当前终端的在线、离线数量及列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termStaNumLst", method = RequestMethod.GET)
    public TermStaLst termStaNumLst(@ApiParam(name = "term_lst", value = "终端列表") @RequestParam(value = "term_lst") String term_lst) {
        return  appService.getTermStaNumLst(term_lst);
    }


    /**
     * 查看某生产单元设备状态
     * @param cell_id 生产单元ID
     * @return
     */
    @ApiOperation(value = "查询设备操作日志" ,  notes="查询设备操作日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/equLogQuery", method = RequestMethod.GET)
    public Collection<EqpLogRsp> equLogQuery(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") Integer cell_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) {
        return  appService.onCellEqpLog(cell_id,start_time,end_time);
    }

    /**
     * 查看某生产单元历史数据
     * @param cell_id 生产单元ID
     * @return
     */
    //查看数据
    @ApiOperation(value = "查询历史数据" ,  notes="查询历史数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/hisQuery", method = RequestMethod.GET)
    public List<EnviHisAggrRsp> cell_history(
            @ApiParam(name = "cell_id", value = "生产单元ID") @RequestParam(value = "cell_id") Integer cell_id,
            @ApiParam(name = "all_env", value = "是否呈现养殖户") @RequestParam(value="all_env",defaultValue="true", required = false) boolean all_env,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return appService.onCellHisSmryQuery(cell_id, all_env,start_time, end_time);
    }

    /**
     * 查询终端参数历史数据
     * @param term_id 生产单元ID
     * @return
     */
    //查看数据
    @ApiOperation(value = "查询终端参数历史数据" ,  notes="查询终端参数历史数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/termtypeHisQuery", method = RequestMethod.GET)
    public EnviHisAggrRsp.SNTV termtype_history(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "type", value = "参数类型") @RequestParam(value="type") String type,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return appService.hisByTermType(term_id, type,start_time, end_time);
    }


    /**
     * 查看终端电量的历史数据
     * @param term_id 终端ID
     * @return
     */
    //查看数据
    @ApiOperation(value = "查询终端电量历史数据" ,  notes="查询终端电量历史数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/voltHisQuery", method = RequestMethod.GET)
    public AppServiceImpl.TermVoltHis volt_history(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return appService.ontermVoltHisQuery(term_id, start_time, end_time);
    }


    /**
     * 查看某生产单元历史数据对比（按照采集参数分组展示）
     * @param cell_ids 生产单元集合
     * @param snp_types 采集参数列表
     * @param start_time 开始时间
     * @param end_time 结束时间
     * @return
     */
    //查看数据
    @ApiOperation(value = "查询历史数据曲线对比" ,  notes="查询历史数据曲线对比")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/hisAggrQuery", method = RequestMethod.GET)
    public List<EnviHisAggrRsp> cellHistoryAggr(
            @ApiParam(name = "cell_ids", value = "生产单元列表") @RequestParam(value = "cell_ids") String cell_ids,
            @ApiParam(name = "snp_types", value = "采集参数列表") @RequestParam(value = "snp_types") String snp_types,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return appService.onCellHisAggrQuery(cell_ids, snp_types, start_time, end_time);
    }


    /**
     * 导出终端的某环境参数历史数据
     * @param term_id 终端ID
     * @param snp_types 采集参数列表
     * @param start_time 开始时间
     * @param end_time 结束时间
     * @return
     */
    //查看数据
    @ApiOperation(value = "导出终端参数历史数据" ,  notes="导出终端参数历史数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/exportTermParam", method = RequestMethod.GET)
    public Map<String, Map<String, Float>> exportTermParam(
            @ApiParam(name = "term_id", value = "终端ID") @RequestParam(value = "term_id") Integer term_id,
            @ApiParam(name = "snp_types", value = "采集参数列表") @RequestParam(value = "snp_types") String snp_types,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return appService.exportTermParam(term_id, snp_types, start_time, end_time);
    }


    /**
     * 查看塘口告警信息
     * @param cell_id 塘口ID
     * @param start_time 开始时间
     * @param end_time 结束时间
     * @return
     */
    //查看数据
    @ApiOperation(value = "查询塘口告警日志" ,  notes="查询塘口告警日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/warnLogQuery", method = RequestMethod.GET)
    public List<WarnDO> warnLogQury(
            @ApiParam(name = "cell_id", value = "终端ID") @RequestParam(value = "cell_id") Integer cell_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return envCtlService.warnQuery(cell_id, start_time, end_time);
    }


    /**
     * 查看塘口设备自动控制日志
     * @param cell_id 塘口ID
     * @param start_time 开始时间
     * @param end_time 结束时间
     * @return
     */
    //查看数据
    @ApiOperation(value = "查看塘口设备自动控制日志" ,  notes="查看塘口设备自动控制日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/autoActLogQuery", method = RequestMethod.GET)
    public List<AutoActionDO> autoActLogQury(
            @ApiParam(name = "cell_id", value = "终端ID") @RequestParam(value = "cell_id") Integer cell_id,
            @ApiParam(name = "start_time", value = "开始时间") @RequestParam(value = "start_time") String start_time,
            @ApiParam(name = "end_time", value = "结束时间") @RequestParam(value = "end_time") String end_time) throws Exception{
        //调用service
        return envCtlService.autoActQuery(cell_id, start_time, end_time);
    }


    /**
     * 设备控制请求
     * @param equip_id 设备ID
     * @param ison_fg 启动标志
     * @return
     */
    @ApiOperation(value = "控制设备启停" ,  notes="控制某个设备启停")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "该参数值（value='Bearer {token}'）在request header中", paramType ="header", required = false, dataType = "String")
    })
    @RequestMapping(value = "/control", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<CtlRspMsg> control(
            @ApiParam(name = "appusrid", value = "用户ID") @RequestParam("appusrid") Integer appusrid,
            @ApiParam(name = "equip_id", value = "被控设备ID") @RequestParam("equip_id") Integer equip_id,
            @ApiParam(name = "ison_fg", value = "命令 0:关，1:开") @RequestParam("ison_fg") int ison_fg){
        DeferredResult<CtlRspMsg> result = new DeferredResult<CtlRspMsg>();
        responseMap.put(equip_id, result);//把请求响应的DeferredResult实体放到第一个响应map中
        appService.onCntrl(appusrid,equip_id,ison_fg);
        return result;
    }




    /**
     * 控制设备返回响应
     * @param equip_id 设备ID
     * @return
     */
    public synchronized CtlRspMsg controlReturn(Integer equip_id){//控制成功的要执行函数
        CtlRspMsg rspMsg=new CtlRspMsg(equip_id, true, "success");//自定义控制响应数据
        DeferredResult<CtlRspMsg> result = responseMap.get(equip_id);
        if (result==null){
            rspMsg.setIsctlsuss(false);
            rspMsg.setMsg(equip_id+"not existed in map.");
            return rspMsg;
        }
        result.setResult(rspMsg);//设置DeferredResult的结果值，设置之后，它对应的请求进行返回处理
        responseMap.remove(equip_id);//返回map删除
        log.info("终端响应成功,调用完成");
        return rspMsg;
    }

    /**
     * 控制设备超时
     * @param equip_id 设备ID
     * @return
     */
    public synchronized CtlRspMsg controlTimeout(Integer equip_id){//控制超时的要执行函数
        CtlRspMsg rspMsg=new CtlRspMsg(equip_id, false, "connClose");
        DeferredResult<CtlRspMsg> result = responseMap.get(equip_id);
        if (result==null){
            rspMsg.setIsctlsuss(false);
            rspMsg.setMsg(equip_id+"not existed in map.");
            return rspMsg;
        }

        result.setResult(rspMsg);//设置DeferredResult的结果值，设置之后，它对应的请求进行返回处理
        responseMap.remove(equip_id);//返回map删除
        log.info("终端超时未响应, 调用完成");
        return rspMsg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class CtlRspMsg {
        int equipid;
        boolean isctlsuss;
        String msg;
    }

}
