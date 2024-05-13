package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.common.BackupResult;
import com.linkcircle.dbmanager.entity.BackupConfig;
import com.linkcircle.dbmanager.entity.ClearDataConfig;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.MysqlUser;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/24 14:35
 */

public interface BackupClearDataService {

    /**
     * 校验软件安装情况
     * @param backupConfig
     * @return
     */
    void checkSoftInstallResult(BackupConfig backupConfig,DatasourceProp prop);

    /**
     * 校验软件安装情况
     * @param backupConfig
     * @param prop
     * @return
     */
    Result checkSoftInstall(BackupConfig backupConfig,DatasourceProp prop) ;

    /**
     * 恢复准备
     * @param backupConfig
     * @return
     */
    Result recoverPrepare(BackupConfig backupConfig,DatasourceProp prop);

    /**
     * 备份操作
     * @param backupConfig
     * @return
     */
    BackupResult doBackup(BackupConfig backupConfig,DatasourceProp prop, boolean checkDate);

    /**
     * 软件安装
     * @param type
     * @return
     */
    Result installSoftware(BackupConfig backupConfig,DatasourceProp prop,String type);

    /**
     *
     * @param backupConfig
     * @return
     */
    Result testSsh(BackupConfig backupConfig);

    /**
     * 备份ibd和frm文件
     * @param clearDataConfig
     * @param connection
     * @param clearTableName
     * @param checkTime
     * @return
     */
    Result backupIbdAndFrm(ClearDataConfig clearDataConfig,DatasourceProp datasourceProp, Connection connection, List<String> clearTableName, boolean checkTime);

    /**
     * 获取ibd、frm文件
     */
    Page<Map<String, String>> getIbdFrm(Page<Map<String, String>> page, DatasourceProp datasourceProp, MysqlUser mysqlUser, String ibdfrmPath);
}
