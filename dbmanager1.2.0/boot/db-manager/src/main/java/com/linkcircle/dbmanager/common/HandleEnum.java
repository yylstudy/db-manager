package com.linkcircle.dbmanager.common;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/18 15:46
 */
public enum HandleEnum {
    UNDO(0,"未执行"),
    FAIL(1,"执行失败"),
    SUCCESS(2,"执行成功");
    private Integer code;
    private String desc;

    HandleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
