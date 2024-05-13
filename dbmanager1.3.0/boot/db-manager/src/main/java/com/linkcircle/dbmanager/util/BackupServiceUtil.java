package com.linkcircle.dbmanager.util;

import com.linkcircle.boot.service.ApplicationContextSupport;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.service.BackupClearDataService;
import com.linkcircle.dbmanager.service.impl.K8sBackupClearDataServiceImpl;
import com.linkcircle.dbmanager.service.impl.SshBackupClearDataServiceImpl;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/24 15:27
 */

public class BackupServiceUtil {
    public static BackupClearDataService getBackupService(String isK8s){
        if(CommonConstant.DATASOURCE_K8S.equals(isK8s)){
            return ApplicationContextSupport.getBean(K8sBackupClearDataServiceImpl.class);
        }
        return ApplicationContextSupport.getBean(SshBackupClearDataServiceImpl.class);
    }
}
