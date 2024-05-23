package com.linkcircle.dbmanager.common;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/23 10:34
 */
public enum MysqlVersionEnum {
    FIVE("5"),
    EIGHT("8");
    private String code;

    MysqlVersionEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
