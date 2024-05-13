package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.entity.MongodbProp;
import com.linkcircle.dbmanager.service.JobInfoService;
import com.linkcircle.dbmanager.service.MongodbPropService;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/mongodb")
public class MongodbPropController {
    @Autowired
    private MongodbPropService mongodbPropService;
    @Autowired
    private JobInfoService jobInfoService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<MongodbProp>> queryPageList(MongodbProp mongodbProp, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<MongodbProp> page = new Page<MongodbProp>(pageNo, pageSize);
        IPage<MongodbProp> pageList = mongodbPropService.queryPage(page, mongodbProp);
        return Result.OK(pageList);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestBody MongodbProp prop) {
        Session session = null;
        try{
            session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),prop.getSshUser(),prop.getSshPassword());
            prop.setSshPassword(PwdUtil.encryptPwd(prop.getSshPassword()));
            prop.setPassword(StringUtils.isEmpty(prop.getPassword())?"":PwdUtil.encryptPwd(prop.getPassword()));
            mongodbPropService.save(prop);
            return Result.OK();
        }catch (Exception e){
            log.error("add error",e);
            return Result.error("添加mongodb失败，请联系管理员"+e.getMessage());
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<?> edit(@RequestBody MongodbProp mongodbProp) {
        Session session = null;
        try{
            if(StringUtils.isNotBlank(mongodbProp.getSshPassword())){
                session = SshUtil.getSession(mongodbProp.getIp(),mongodbProp.getSshPort(),
                        mongodbProp.getSshUser(),mongodbProp.getSshPassword());
            }
            if(StringUtils.isNotBlank(mongodbProp.getPassword())){
                mongodbProp.setPassword(PwdUtil.encryptPwd(mongodbProp.getPassword()));
            }
            if(StringUtils.isNotBlank(mongodbProp.getSshPassword())){
                mongodbProp.setSshPassword(PwdUtil.encryptPwd(mongodbProp.getSshPassword()));
            }
            mongodbPropService.updateById(mongodbProp);
            return Result.OK();
        }catch (Exception e){
            log.error("edit error",e);
            return Result.error("添加mongodb失败，请联系管理员"+e.getMessage());
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }

    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        LambdaQueryWrapper<JobInfo> wrapper  = Wrappers.lambdaQuery();
        wrapper.eq(JobInfo::getConfigId,id);
        wrapper.eq(JobInfo::getTaskType,"3");
        int count = jobInfoService.count(wrapper);
        if(count>0){
            return Result.error("删除失败，存在关联此数据库的任务");
        }
        mongodbPropService.removeById(id);
        return Result.OK();
    }

}
