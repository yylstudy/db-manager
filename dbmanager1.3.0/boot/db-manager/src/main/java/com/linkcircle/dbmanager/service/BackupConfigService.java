package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.common.BackupResult;
import com.linkcircle.dbmanager.entity.BackupConfig;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface BackupConfigService extends IService<BackupConfig> {
    /**
     * 校验软件安装状态，激活状态
     * @param id
     * @return
     */
    Result active(Long id);

    /**
     * 校验软件安装情况
     * @param backupConfig
     * @return
     */
    Result checkSoftInstall(BackupConfig backupConfig);

    /**
     * 恢复准备
     * @param backupConfig
     * @return
     */
    Result recoverPrepare(BackupConfig backupConfig);

    /**
     * 备份操作
     * @param backupConfig
     * @return
     */
    BackupResult doBackup(BackupConfig backupConfig,boolean checkDate);

    /**
     * 软件安装
     * @param id
     * @param type
     * @return
     */
    Result installSoftware(Long id,  String type);

    Result testmysql(BackupConfig backupConfig);
    Result testSsh( BackupConfig backupConfig);

    IPage<BackupConfig> queryPage(Page<BackupConfig> page, BackupConfig backupConfig);
}
