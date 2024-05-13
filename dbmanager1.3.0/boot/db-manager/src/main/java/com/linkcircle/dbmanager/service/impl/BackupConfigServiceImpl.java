package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.aspect.annotation.JRepeat;
import com.linkcircle.dbmanager.common.BackupResult;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.entity.BackupConfig;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.mapper.BackupConfigMapper;
import com.linkcircle.dbmanager.service.BackupClearDataService;
import com.linkcircle.dbmanager.service.BackupConfigService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.dbmanager.util.BackupServiceUtil;
import com.linkcircle.dbmanager.util.JdbcUtil;
import com.linkcircle.dbmanager.util.PwdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service("backupConfigServiceImpl")
@Slf4j
public class BackupConfigServiceImpl extends ServiceImpl<BackupConfigMapper, BackupConfig> implements BackupConfigService {
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Override
    public Result active(Long id) {
        BackupConfig backupConfig = this.getById(id);
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        Result result = backupClearDataService.checkSoftInstall(backupConfig,prop);
        List<Boolean> list = (List<Boolean>)result.getResult();
        boolean allSuccess = list.stream().allMatch(a->a);
        if(allSuccess){
            backupConfig.setStatus(1);
            this.updateById(backupConfig);
        }
        list.add(0, CommonConstant.DATASOURCE_K8S.equals(prop.getIsk8s()));
        return result;
    }


    @Override
    public Result checkSoftInstall(BackupConfig backupConfig) {
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.checkSoftInstall(backupConfig,prop);
    }


    @Override
    public Result recoverPrepare(BackupConfig backupConfig) {
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.recoverPrepare(backupConfig,prop);
    }


    @Override
    @JRepeat(lockTime = 30,lockKey = "doBackup:${backupConfig.propId}")
    public BackupResult doBackup(BackupConfig backupConfig,boolean checkTime) {
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.doBackup(backupConfig,prop,checkTime);
    }


    @Override
    public Result installSoftware(Long id, String type) {
        BackupConfig backupConfig = this.getById(id);
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.installSoftware(backupConfig,prop,type);
    }


    @Override
    public Result testmysql(BackupConfig backupConfig) {
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        String url = "jdbc:mysql://"+prop.getIp()+":"+prop.getPort();
        String password = PwdUtil.decryptPwd(prop.getPassword());
        boolean getConnection = JdbcUtil.connectionMysql(url, prop.getUser(),password);
        if(getConnection){
            return Result.OK("mysql连接成功",null);
        }else{
            return Result.error("mysql连接失败");
        }
    }

    @Override
    public Result testSsh(BackupConfig backupConfig) {
        DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
        BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
        return backupClearDataService.testSsh(backupConfig);
    }

    @Override
    public IPage<BackupConfig> queryPage(Page<BackupConfig> page, BackupConfig backupConfig) {
        return this.baseMapper.queryPage(page, backupConfig);
    }

}
