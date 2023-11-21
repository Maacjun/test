package edu.jit.nsi.iot_ms.serviceimpl.custom;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import edu.jit.nsi.iot_ms.commons.pages.PageQO;
import edu.jit.nsi.iot_ms.commons.pages.PageVO;
import edu.jit.nsi.iot_ms.domain.CellDO;
import edu.jit.nsi.iot_ms.mapper.CellDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @packageName: com.jit.iot.service.Impl
 * @className: CellServiceImpl
 * @Description:
 * @author: xxz
 * @date: 2019/7/25 10:12
 */
@Slf4j
@Service
public class CellServiceImpl {
    @Autowired
    TerminalServiceImpl terminalService;

    @Resource
    CellDAO cellDAO;

    /**
     * 新增生产单元
     * @param type        农业类型
     * @param product     养殖对象
     * @param cell_name   生产单元名字
     * @param user_name   归属用户
     * @return
     */
    public CellDO add_cell(float length, float width, double longitude,
                            double latitude, String type, String product, String cell_name, String user_name) {
//        String opter = SecurityContextHolder.getContext().getAuthentication().getName();
//        log.debug("用户{}新增{}养殖生产单元, 生产单元名:{}, 生产单元归属:{}", opter, type, cell_name, user_name);
        CellDO cellDO = new CellDO(length, width, longitude, latitude, type, product, cell_name,user_name);
        int ret = cellDAO.insert(cellDO);
        if(ret < 0){
            return null;
        } else {
            return cellDO;
        }
    }

    /**
     * 更新生产单元
     * @param type        农业类型
     * @param product     养殖对象
     * @param cell_name   生产单元名字
     * @param user_name   归属用户
     * @return
     */
    public boolean update_cell(int cellid, float length, float width, double longitude,
                            double latitude, String type, String product, String cell_name, String user_name) {
//        String opter = SecurityContextHolder.getContext().getAuthentication().getName();
//        log.debug("用户{}更新{}养殖生产单元, 生产单元名:{}, 生产单元归属:{}", opter, type, cell_name, user_name);
        CellDO cell = new CellDO(cellid, length, width, longitude, latitude, type, product, cell_name,user_name);
        int ret = cellDAO.updateById(cell);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除生产单元
     * @return
     */
    public boolean delete_cell(int cellid) {
//        String opter = SecurityContextHolder.getContext().getAuthentication().getName();
//        log.debug("用户{}删除{}养殖生产单元, 生产单元名:{}, 生产单元归属:{}", opter);
        int ret = cellDAO.deleteById(cellid);
        if(ret < 0){
            return false;
        } else {
            return true;
        }
    }

    /**
     * 查询用户的所有生产单元
     * @param usrname   用户名
     * @return
     */
    public PageVO<CellDO> getUsrCells(PageQO pageQO, String usrname){
        Page<CellDO> page = PageHelper.startPage(pageQO.getPageNum(),pageQO.getPageSize());
        List<CellDO> cells = cellDAO.selectList(new EntityWrapper<CellDO>().eq("username",usrname));
        return PageVO.build(page);
    }

    /**
     * 查询用户的所有生产单元
     * @param cell_ids  单元列表
     * @return
     */
    public List<CellDO> getCellByIds(String cell_ids){
        List<Integer> cellids = new ArrayList<>();
        String[] cids = cell_ids.split(",");
        for(String id: cids){
            cellids.add(Integer.parseInt(id));
        }
        List<CellDO> cells = cellDAO.selectBatchIds(cellids);
        return cells;
    }

    //根据cell_id得到Cell详细信息
    public CellDO getCellsById(int cell_id){
        CellDO cell= new CellDO(cell_id);
        return cellDAO.selectOne(cell);
    }
}
