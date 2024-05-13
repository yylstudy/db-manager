package com.linkcircle.dbmanager.common;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/18 15:23
 */
public class CommonConstant {
    public static final String SEPARATOR = "/";
    public static final Integer CONTAIN_TIME = 0;
    public static final String FULLPREFIX = "full";
    public static final String INCRPREFIX = "incr";
    public static final String ALLRPREFIX = "all";
    public static final String SUCCESS_FLAG = "completed OK";
    public static final String INSTALL_XTRABACKUP = "xtrabackup";
    public static final String INSTALL_QPRESS = "qpress";
    public static final String LOCALHOST_FILE = "0";
    /**
     * 任务变更通知角色
     */
    public static final String SUPER_ADMIN_ROLE = "superadmin";
    /**
     * 巡检员角色
     */
    public static final String MONITOR_ROLE = "monitor";

    /**
     * 密码、用户变更发送角色
     */
    public static final String PWD_CHANGE_ROLE = "pwd_change";
    public static final String IBD_SUFFIX = ".ibd";
    public static final String FRM_SUFFIX = ".frm";
    public static final String MYSQL_PRIV_Y = "Y";

    public static final List<String> DEFAULT_DATABASES = Arrays.asList("information_schema","mysql","performance_schema","sys");

    public static final String ROOT_USER = "root";
    /**
     * exec success标识
     */
    public static final Integer EXEC_SUCCESS = 0;
    /**
     * k8s数据源
     */
    public static final String DATASOURCE_K8S = "1";
    /**
     * k8s数据源
     */
    public static final String XTRABACKUP_DEFAULT_CONTAINER = "xtrabackup";
    /**
     * k8s mysql默认IP
     */
    public static final String K8S_DEFAULT_MYSQL_IP = "127.0.0.1";
    /**
     * k8s mysql默认端口
     */
    public static final String K8S_DEFAULT_MYSQL_PORT = "3306";
    /**
     * scp传输默认shell名称
     */
    public static final String DEFAULT_SHELL_NAME = "scp.sh";
    /**
     * sftp备份默认目录
     */
    public static final String DEFAULT_BACKUP_DIR = "backupfiles";
    /**
     * EXPECT默认超时时间
     */
    public static final long EXPECT_DEFAULT_TIMEOUT = 6*60*60;
//    public static final long EXPECT_DEFAULT_TIMEOUT = 2*60;
}
