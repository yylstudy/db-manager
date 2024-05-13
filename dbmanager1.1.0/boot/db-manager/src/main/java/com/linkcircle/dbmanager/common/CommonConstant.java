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

}
