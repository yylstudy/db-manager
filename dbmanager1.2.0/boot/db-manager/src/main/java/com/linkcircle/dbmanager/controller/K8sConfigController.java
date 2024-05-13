package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.system.query.QueryGenerator;
import com.linkcircle.dbmanager.config.K8sClientManager;
import com.linkcircle.dbmanager.entity.JobLog;
import com.linkcircle.dbmanager.entity.K8sConfig;
import com.linkcircle.dbmanager.service.K8sConfigService;
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
@RequestMapping("/k8sconfig")
public class K8sConfigController {
    @Autowired
    private K8sConfigService k8sConfigService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<K8sConfig>> queryPageList(K8sConfig k8sConfig, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                     @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<K8sConfig>> result = new Result();
        QueryWrapper<K8sConfig> queryWrapper = QueryGenerator.initQueryWrapper(k8sConfig, req.getParameterMap());
        Page<K8sConfig> page = new Page(pageNo, pageSize);
        IPage<K8sConfig> pageList = k8sConfigService.page(page, queryWrapper);
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
    public Result<JobLog> add(@RequestBody K8sConfig k8sConfig) {
        k8sConfigService.add(k8sConfig);
        return Result.OK();
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<JobLog> edit(@RequestBody K8sConfig k8sConfig) {
        k8sConfigService.edit(k8sConfig);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        K8sClientManager.delete(Long.parseLong(id));
        k8sConfigService.removeById(id);
        return Result.OK();
    }



}
