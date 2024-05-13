package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.common.TaskTypeEnum;
import com.linkcircle.dbmanager.entity.ClearDataConfig;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.entity.MysqlUser;
import com.linkcircle.dbmanager.service.ClearDataConfigService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.dbmanager.service.JobInfoService;
import com.linkcircle.dbmanager.service.MysqlUserService;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/clearData")
public class ClearDataConfigController {
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private JobInfoService jobInfoService;
    @Autowired
    private MysqlUserService mysqlUserService;
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
    public Result<IPage<ClearDataConfig>> queryPageList(ClearDataConfig clearDataConfig, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<ClearDataConfig> page = new Page<ClearDataConfig>(pageNo, 10000);
        IPage<ClearDataConfig> pageList = clearDataConfigService.queryPage(page, clearDataConfig);
        return Result.OK(pageList);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Object> add(@RequestBody ClearDataConfig clearDataConfig) {
        checkIbdfrm(clearDataConfig);
        clearDataConfigService.add(clearDataConfig);
        return Result.OK();
    }

    private void checkIbdfrm(ClearDataConfig clearDataConfig){
        Session session = null;
        try{
            Long mysqlUserId = clearDataConfig.getMysqlUserId();
            MysqlUser mysqlUser = mysqlUserService.getById(mysqlUserId);
            DatasourceProp datasourceProp = datasourcePropService.getById(mysqlUser.getPropId());
            session = SshUtil.getSession(datasourceProp.getIp(),datasourceProp.getSshPort(),
                    datasourceProp.getSshUser(), PwdUtil.decryptPwd(datasourceProp.getSshPassword()));
            Integer execStatus = SshUtil.getExecStatus(session,"cd "+clearDataConfig.getIbdFrmDir());
            if(execStatus==null||execStatus!=0){
                throw new BusinessException("ibdfrm存储目录"+clearDataConfig.getIbdFrmDir()+"不存在，请检查配置");
            }
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }

    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "testConnection", method = RequestMethod.POST)
    public Result testConnection(@RequestParam Long id) {
        boolean getConnection = clearDataConfigService.testConnection(id);
        if(getConnection){
            return Result.OK();
        }else{
            return Result.error("数据库配置错误");
        }
    }

    /**
     *   获取清理表名
     * @param
     * @return
     */
    @RequestMapping(value = "getClearTableName")
    public Result getClearTableName(@RequestParam Long id) {
        List<String> set = clearDataConfigService.getClearTableName(id);
        return Result.OK(set);
    }
    /**
     *   根据任务ID获取清理表名
     * @param
     * @return
     */
    @RequestMapping(value = "getClearTableNameByJobId")
    public Result getClearTableNameByJobId(@RequestParam Long jobId) {
        JobInfo jobInfo = jobInfoService.getById(jobId);
        List<String> set = clearDataConfigService.getClearTableName(jobInfo.getConfigId());
        return Result.OK(set);
    }

    @RequestMapping(value = "testSsh", method = RequestMethod.POST)
    public Result testSsh(@RequestBody ClearDataConfig clearDataConfig) {
        return clearDataConfigService.testSsh(clearDataConfig);
    }

    @RequestMapping(value = "testmysql", method = RequestMethod.POST)
    public Result testmysql(@RequestBody ClearDataConfig clearDataConfig) {
        return clearDataConfigService.testmysql(clearDataConfig);
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<ClearDataConfig> edit(@RequestBody ClearDataConfig clearDataConfig) {
        checkIbdfrm(clearDataConfig);
        clearDataConfigService.edit(clearDataConfig);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") Long id) {
        LambdaQueryWrapper<JobInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(JobInfo::getConfigId,id);
        wrapper.eq(JobInfo::getTaskType, TaskTypeEnum.CLEAR_DATA.getCode());
        int count = jobInfoService.count(wrapper);
        if(count>0){
            return Result.error("删除失败，存在关联此配置的数据清理配置");
        }
        clearDataConfigService.delete(id);
        return Result.OK();
    }

}
