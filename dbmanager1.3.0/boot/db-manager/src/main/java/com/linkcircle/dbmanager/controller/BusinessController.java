package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.Business;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.service.BusinessService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/business")
public class BusinessController {
    @Autowired
    private BusinessService businessService;
    @Autowired
    private ISysUserService sysUserService;
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
    public Result<IPage<Business>> queryPageList(Business business, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<Business>> result = new Result();
        LambdaQueryWrapper<Business> wrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(business.getBusinessName())){
            wrapper.like(Business::getBusinessName,business.getBusinessName());
        }
        Page<Business> page = new Page<Business>(pageNo, pageSize);
        IPage<Business> pageList = businessService.page(page, wrapper);
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
    public Result<Business> add(@RequestBody Business business) {
        setRealName(business);
        businessService.save(business);
        return Result.OK();
    }

    private void setRealName(Business business){
        String userId = business.getUserId();
        String userName = Arrays.stream(userId.split(",")).map(sysUserService::getById)
                .map(SysUser::getRealname).collect(Collectors.joining(","));
        business.setUserName(userName);
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<Business> edit(@RequestBody Business business) {
        setRealName(business);
        businessService.updateById(business);
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
        wrapper.eq(DatasourceProp::getBusinessId,id);
        int count = datasourcePropService.count(wrapper);
        if(count>0){
            return Result.error("删除失败，存在关联此项目的数据源配置");
        }
        businessService.removeById(id);
        return Result.OK();
    }

}
