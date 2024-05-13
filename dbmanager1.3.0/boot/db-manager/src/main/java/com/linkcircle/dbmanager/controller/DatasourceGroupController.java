package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.system.query.QueryGenerator;
import com.linkcircle.dbmanager.entity.DatasourceGroup;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.service.DatasourceGroupService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/19 16:34
 */
@RestController
@Slf4j
@RequestMapping("/datasourceGroup")
public class DatasourceGroupController {
    @Autowired
    private DatasourceGroupService datasourceGroupService;
    @Autowired
    private DatasourcePropService datasourcePropService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<DatasourceGroup>> queryPageList(DatasourceGroup datasourceGroup, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<DatasourceGroup>> result = new Result();
        QueryWrapper<DatasourceGroup> queryWrapper = QueryGenerator.initQueryWrapper(datasourceGroup, req.getParameterMap());
        Page<DatasourceGroup> page = new Page(pageNo, pageSize);
        IPage<DatasourceGroup> pageList = datasourceGroupService.page(page, queryWrapper);
        for (DatasourceGroup record : pageList.getRecords()) {
            List<DatasourceProp> list = datasourcePropService.findByGroupId(record.getId());
            String ids = list.stream().map(DatasourceProp::getId).map(String::valueOf).collect(Collectors.joining(","));
            String ips = list.stream().map(DatasourceProp::getIp).collect(Collectors.joining(","));
            record.setDatasourcePropsIds(ids);
            record.setIps(ips);
        }
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
    public Result<Object> add(@RequestBody DatasourceGroup datasourceGroup) {
        return datasourceGroupService.add(datasourceGroup);
    }


    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        return datasourceGroupService.delete(Long.parseLong(id));
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<Object> edit(@RequestBody DatasourceGroup datasourceGroup) {
        return datasourceGroupService.edit(datasourceGroup);
    }

}
