package com.linkcircle.dbmanager.common;

import java.util.Arrays;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/23 10:34
 */
public enum ClearTypeEnum {
    DELETE("delete","delete from "),
    TRUNCATE("truncate","truncate");
    private String code;
    private String clearPrefix;
    ClearTypeEnum(String code, String clearPrefix) {
        this.code = code;
        this.clearPrefix = clearPrefix;
    }
    public static ClearTypeEnum getEnum(String operate){
        return Arrays.stream(ClearTypeEnum.values()).filter(enu->enu.code.equals(operate)).findFirst().orElse(null);
    }

    public String getCode() {
        return code;
    }

    public String getClearPrefix() {
        return clearPrefix;
    }
}
