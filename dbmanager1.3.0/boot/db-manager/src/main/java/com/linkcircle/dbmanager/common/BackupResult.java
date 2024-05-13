package com.linkcircle.dbmanager.common;

import lombok.Data;

/**
 * @description: 备份结果
 * @author: yangyonglian
 * @time: 2021/9/22 15:01
 */
@Data
public class BackupResult {
    /**
     * 备份是否成功
     */
    private boolean success;
    /**
     * 备份类型
     */
    private BackupType backupType = BackupType.UNKNOW;
    /**
     * 备份生成的文件，这个只有在备份成功时才有值
     */
    private String backupDir;
    /**
     * 备份日志
     */
    private String msg;
    /**
     * 新生成的tar包
     */
    private String newTarFile;
    /**
     * 删除掉的tar包
     */
    private String deleteTarFile;
    /**
     * 远程存储是否成功
     */
    private boolean remoteStoreSuccess;
    /**
     * 开启远程存储
     */
    private boolean enableRemoteStore;
    /**
     * 远程存储耗时
     */
    private long remoteStoreTimeCost;
    /**
     * scp远程存储日志
     */
    private String remoteStoreMsg;
    /**
     * 删除远程文件
     */
    private String deleteRemoteFile;
}
