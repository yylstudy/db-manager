package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.system.query.QueryGenerator;
import com.linkcircle.dbmanager.entity.ComputerRoom;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.service.ComputerRoomService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/computerRoom")
public class ComputerRoomController {
    @Autowired
    private ComputerRoomService computerRoomService;
    @Autowired
    private DatasourcePropService datasourcePropService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<ComputerRoom>> queryPageList(ComputerRoom computerRoom, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                     @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<ComputerRoom>> result = new Result();
        QueryWrapper<ComputerRoom> queryWrapper = QueryGenerator.initQueryWrapper(computerRoom, req.getParameterMap());
        Page<ComputerRoom> page = new Page<ComputerRoom>(pageNo, pageSize);
        IPage<ComputerRoom> pageList = computerRoomService.page(page, queryWrapper);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<ComputerRoom> add(@RequestBody ComputerRoom computerRoom) {
        computerRoomService.save(computerRoom);
        return Result.OK();
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<ComputerRoom> edit(@RequestBody ComputerRoom computerRoom) {

        computerRoomService.updateById(computerRoom);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        LambdaQueryWrapper<DatasourceProp> wrapper  = Wrappers.lambdaQuery();
        wrapper.eq(DatasourceProp::getComputerRoomId,id);
        int count = datasourcePropService.count(wrapper);
        if(count>0){
            return Result.error("删除失败，存在关联此机房的数据源配置");
        }
        computerRoomService.removeById(id);
        return Result.OK();
    }

}
