package com.linkcircle.dbmanager.common;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/22 15:19
 */
public enum BackupType {
    UNKNOW("unknow","未知"),
    FULL("full","全量备份"),
    INCR("incr","增量备份");
    private String code;
    private String desc;

    BackupType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
