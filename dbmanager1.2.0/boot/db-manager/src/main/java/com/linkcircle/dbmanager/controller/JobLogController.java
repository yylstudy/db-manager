package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.JobLog;
import com.linkcircle.dbmanager.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/joblog")
public class JobLogController {
    @Autowired
    private JobLogService jobLogService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<JobLog>> queryPageList(JobLog jobLog, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<JobLog> page = new Page<JobLog>(pageNo, pageSize);
        IPage<JobLog> pageList = jobLogService.queryPage(page, jobLog);
        return Result.OK(pageList);
    }

    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/getIbdFrm", method = RequestMethod.GET)
    public Result<IPage<Map<String,String>>> getIbdFrm(@RequestParam(required = false) Long id, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<Map<String,String>> page = new Page<Map<String,String>>(pageNo, pageSize);
        if(id==null){
            return Result.OK(page);
        }
        IPage<Map<String,String>> pageList = jobLogService.getIbdFrm(page, id);
        return Result.OK(pageList);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<JobLog> add(@RequestBody JobLog jobLog) {
        jobLogService.save(jobLog);
        return Result.OK();
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<JobLog> edit(@RequestBody JobLog jobLog) {
        jobLogService.updateById(jobLog);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        jobLogService.removeById(id);
        return Result.OK();
    }
    /**
     *   根据ID查找
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id") String id) {
        return Result.OK(jobLogService.getById(id));
    }
}
