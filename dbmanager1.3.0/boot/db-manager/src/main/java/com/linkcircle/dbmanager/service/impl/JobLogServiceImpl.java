package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.JobLogMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.BackupServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLog> implements JobLogService {
    @Autowired
    private JobInfoService jobInfoService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Override
    public IPage<JobLog> queryPage(Page<JobLog> page, JobLog jobLog) {
        return this.baseMapper.queryPage(page,jobLog);
    }

    @Override
    public IPage<Map<String, String>> getIbdFrm(Page<Map<String, String>> page, Long id) {
        JobLog jobLog = this.getById(id);
        Long jobId = jobLog.getJobId();
        JobInfo jobInfo = jobInfoService.getById(jobId);
        if(jobInfo==null){
            return new Page();
        }
        ClearDataConfig clearDataConfig = clearDataConfigService.getById(jobInfo.getConfigId());
        if(clearDataConfig==null){
            return new Page();
        }
        MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
        DatasourceProp prop = datasourcePropService.getById(mysqlUser.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.getIbdFrm(page,prop,mysqlUser,jobLog.getIbdfrmPath());
    }


}
