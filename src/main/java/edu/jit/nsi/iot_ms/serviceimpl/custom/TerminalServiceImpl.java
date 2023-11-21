package edu.jit.nsi.iot_ms.serviceimpl.custom;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import edu.jit.nsi.iot_ms.commons.pages.PageQO;
import edu.jit.nsi.iot_ms.commons.pages.PageVO;
import edu.jit.nsi.iot_ms.config.SensorCmdCfg;
import edu.jit.nsi.iot_ms.domain.TermDO;
import edu.jit.nsi.iot_ms.mapper.TermDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TerminalServiceImpl {
    @Autowired
    SensorCmdCfg sensorCfg;
    @Autowired
    TermDAO termDAO;
    @Autowired
    SensorServiceImpl sensorService;
    @Autowired
    EquipServiceImpl equipService;

    /**
     * 新增用户终端
     * @param type   终端类型
     * @param name   终端名字
     * @param deveui 串号
     * @param user   终端归属用户
     * @return
     */
    @SelectKey(statement="select LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
    public TermDO addTerm(int type, String deveui, String name, String user, String manu, String prod, int period, int preh, int plat){
        //需要判断是否有权限操作
        //String opter = SecurityContextHolder.getContext().getAuthentication().getName();
        //log.debug("用户{}新增{}类型终端, deveui:{}, 终端归属:{}", opter, type, deveui, user);
        TermDO term = getTerm(0, deveui, user, name);
        if(term!=null){
            log.error("终端ID:"+term.getId()+" 已存在");
            return null;
        }
//        int period = sensorCfg.getManuPeriod(type);
        term =  new TermDO(type, deveui,user, name, manu, prod,period, preh,plat);
        int ret = termDAO.insert(term);
        if (ret < 0) {
            log.error("添加终端type:{}, name:{}失败", type,name);
            return null;
        } else {
            return term;
        }
    }

    /**
     * 更新用户终端配置
     * @param id     终端ID
     * @param name   终端名字
     * @param deveui 串号
     * @param user   终端归属用户
     * @param period 终端上报周期
     * @return
     */
    public boolean updateTerm(int id, String deveui, String user, String name, int period, int preh, int plat){
        TermDO termDO = termDAO.selectById(id);
        if(deveui!=null)
            termDO.setDeveui(deveui);
        if(user!=null)
            termDO.setUsername(user);
        if(name!=null)
            termDO.setName(name);
        if(period!=0)
            termDO.setDatacycle(period);
        if(preh!=0)
            termDO.setPreheat(preh);
        if(plat!=-1)
            termDO.setToplat(plat);
        int ret = termDAO.updateById(termDO);
        if(ret < 0){
            log.error("更新终端id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除终端
     * @param id     终端ID
     * @return
     */
    public boolean deleteTerm(int id){
        sensorService.deleteSensorsInTerm(id);
        equipService.delEquipsInTerm(id);
        int ret = termDAO.deleteById(id);
        if(ret < 0){
            log.error("删除终端id{}失败!", id);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 分页列出系统下所有终端
     * @return
     */
    public PageVO<TermDO> pglistAllTerms(PageQO pageQO){
        Page<TermDO> page = PageHelper.startPage(pageQO.getPageNum(),pageQO.getPageSize());
        List<TermDO> termDOS = termDAO.selectList(new EntityWrapper<TermDO>());
        return PageVO.build(page);
//        return termDAO.selectList(new EntityWrapper<TermDO>());
    }

    /**
     * 分页查询用户终端
     * @param usrname 用户名称
     * @return
     */
    public PageVO<TermDO> pglistTermsByUsr(PageQO pageQO, String usrname){
        Page<TermDO> page = PageHelper.startPage(pageQO.getPageNum(),pageQO.getPageSize());
        List<TermDO> termDOS =  termDAO.selectList(new EntityWrapper<TermDO>().eq("username", usrname));
        return PageVO.build(page);
    }

    /**
     * 列出系统下所有终端
     * @return
     */
    public List<TermDO> listAllTerm(){
        return termDAO.selectList(new EntityWrapper<TermDO>());
    }

    /**
     * 查询用户终端
     * @param usrname 用户名称
     * @return
     */
    public List<TermDO> listUsrTerm(String usrname){
        return termDAO.selectList(new EntityWrapper<TermDO>().eq("username", usrname));
    }

    /**
     * 根据ID和deveui更新终端在线状态
     * @param deveui 串号
     * @return
     */
    public int getTermIdEui(String deveui){
        TermDO term = getTerm(0, deveui, null,null);
        if(term == null){
            log.error("deveui:{}的终端不存在", deveui);
            return 0;
        }else{
            return term.getId();
        }
    }

    /**
     * 根据ID和deveui获取终端对象
     * @param tid    终端ID
     * @return
     */
    public TermDO getTermById(int tid){
        return termDAO.selectById(tid);
    }


    /**
     * 根据用户终端name和deveui获取终端对象
     * @param tid   终端id
     * @param usrname 用户名字
     * @param name   终端名字
     * @param deveui 串号
     * @return
     */
    private TermDO getTerm(int tid, String deveui, String usrname, String name){
        //需要约束id自增从1开始
        if (tid!=0)
            return getTermById(tid);
        else if(deveui!=null)
            return getTermByEUI(deveui);
        else
            return getTermByName(usrname, name);
    }


    /**
     * 根据deveui获取终端对象
     * @param deveui 串号
     * @return
     */
    private TermDO getTermByEUI(String deveui){
        List<TermDO> termlist;
        if(deveui!=null) {
            termlist = termDAO.selectList(new EntityWrapper<TermDO>().eq("deveui", deveui).last("LIMIT 1"));
            if (termlist != null && !termlist.isEmpty()) {
                return termlist.get(0);
            }
        }
        return null;
    }


    /**
     * 根据ID和deveui获取终端对象
     * @param usr_name   终端名字、用户名字
     * @param term_name   终端名字、用户名字
     * @return
     */
    private TermDO getTermByName(String usr_name, String term_name){
        List<TermDO> termlist;
        termlist = termDAO.selectList(new EntityWrapper<TermDO>().eq("name", term_name).eq("username", usr_name).last("LIMIT 1"));
        if(termlist!=null && !termlist.isEmpty()){
            return termlist.get(0);
        }else{
            return null;
        }
    }

    public int getTermDataCycle(int tid){
        TermDO tmo = termDAO.selectById(tid);
        if(tmo!=null) {
            return tmo.getDatacycle();
        }else{
            return 5;
        }
    }

    public boolean setTermDataCycle(int tid, int dc){
        return updateTerm( tid, null, null, null, dc, 0, -1);
    }

    public int getTermPreHeat(int tid){
        TermDO tmo = termDAO.selectById(tid);
        if(tmo!=null) {
            return tmo.getPreheat();
        }else{
            return 3;
        }
    }

    public int getTermToPlat(int tid){
        TermDO tmo = termDAO.selectById(tid);
        if(tmo!=null) {
            return tmo.getToplat();
        }else{
            return 0;
        }
    }


}
