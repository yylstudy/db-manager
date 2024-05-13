package com.linkcircle.dbmanager.common;

import lombok.Data;

/**
 * @description: 备份结果
 * @author: yangyonglian
 * @time: 2021/9/22 15:01
 */
@Data
public class MongoBackupResult {
    /**
     * 备份是否成功
     */
    private boolean success;
    /**
     * 备份生成的文件，这个只有在备份成功时才有值
     */
    private String backupDir;
    /**
     * 备份日志
     */
    private String msg;

}
