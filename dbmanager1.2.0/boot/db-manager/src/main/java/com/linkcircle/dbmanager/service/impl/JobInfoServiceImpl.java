package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.system.vo.DictModel;
import com.linkcircle.boot.common.system.vo.LoginUser;
import com.linkcircle.boot.service.CommonService;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.TaskTypeEnum;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.JobInfoMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.service.ISysDictService;
import com.linkcircle.system.service.ISysLogService;
import com.linkcircle.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo> implements JobInfoService {
    @Autowired
    private ISysLogService sysLogService;
    @Autowired
    private ISysDictService sysDictService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private JobInfoHisService jobInfoHisService;
    @Autowired
    private BackupConfigService backupConfigService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Autowired
    private ComputerRoomService computerRoomService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Override
    public IPage<JobInfo> queryPage(Page<JobInfo> page, JobInfo jobInfo) {
        return this.baseMapper.queryPage(page,jobInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer triggerStatus) {
        JobInfo jobInfo = this.getById(id);
        jobInfo.setTriggerStatus(triggerStatus);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<DictModel> dictModelList = sysDictService.getDictItems("trigger_status");
        String text = dictModelList.stream().filter(model->model.getValue().equals(String.valueOf(triggerStatus)))
                .findFirst().map(ss->ss.getText()).orElse("");
        String jobName;
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getTaskTypeEnum(jobInfo.getTaskType());
        if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
            jobName = backupConfigService.getById(jobInfo.getConfigId()).getName();
        }else{
            jobName = clearDataConfigService.getById(jobInfo.getConfigId()).getName();
        }
        String logContent = user.getUsername()+text+"了任务："+jobName;
        sysLogService.add(logContent);
        List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.SUPER_ADMIN_ROLE);
        StringBuilder emailer = new StringBuilder();
        if(!sysUserList.isEmpty()){
            for(SysUser sysUser:sysUserList){
                commonService.sendSms(sysUser.getPhone(),logContent);
                emailer.append(sysUser.getEmail()).append(",");
            }
            try{
                commonService.sendMail(emailer.toString(),logContent,logContent,null,null);
            }catch (Exception e){
                log.error("send email error",e);
            }
        }
        this.updateById(jobInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delete(String id) {
        JobInfo jobInfo = this.getById(id);
        JobInfoHis jobInfoHis = new JobInfoHis();
        BeanUtils.copyProperties(jobInfo, jobInfoHis);
        jobInfoHis.setDelTime(new Date());
        jobInfoHisService.save(jobInfoHis);
        this.removeById(id);
        return Result.OK();
    }

    @Override
    public ComputerRoom getComputerRoom(Long id) {
        JobInfo jobInfo = this.getById(id);
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getTaskTypeEnum(jobInfo.getTaskType());
        long propId;
        if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
            BackupConfig backupConfig = backupConfigService.getById(jobInfo.getConfigId());
            propId = backupConfig.getPropId();
        }else if(taskTypeEnum==TaskTypeEnum.CLEAR_DATA){
            ClearDataConfig clearDataConfig = clearDataConfigService.getById(jobInfo.getConfigId());
            propId = mysqlUserService.getById(clearDataConfig.getMysqlUserId()).getPropId();
        }else{
            throw new RuntimeException("无效的任务类型");
        }
        DatasourceProp datasourceProp = datasourcePropService.getById(propId);
        return computerRoomService.getById(datasourceProp.getComputerRoomId());
    }
}
