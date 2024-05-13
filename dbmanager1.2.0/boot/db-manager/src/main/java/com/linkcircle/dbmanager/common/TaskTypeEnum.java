package com.linkcircle.dbmanager.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/17 16:05
 */
public enum TaskTypeEnum {
    BACK_MYSQL("1","mysql备份"),
    CLEAR_DATA("2","数据清理");
    private String code;
    private String desc;

    TaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static Map<String,TaskTypeEnum> map = new HashMap<>();
    static{
        for(TaskTypeEnum taskTypeEnum:values()){
            map.put(taskTypeEnum.code,taskTypeEnum);
        }
    }
    public static TaskTypeEnum getTaskTypeEnum(String code){
        return map.get(code);
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
