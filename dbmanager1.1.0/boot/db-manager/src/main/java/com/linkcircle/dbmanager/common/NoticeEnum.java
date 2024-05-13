package com.linkcircle.dbmanager.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/18 15:46
 */
public enum NoticeEnum {
    ALL("1","全接收"),
    SMS("2","短信"),
    MAIL("3","邮件"),
    NEVER("4","全不接收");
    private String code;
    private String desc;

    NoticeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static Map<String,NoticeEnum> map = new HashMap<>();
    static{
        for(NoticeEnum noticeEnum:values()){
            map.put(noticeEnum.code,noticeEnum);
        }
    }
    public static NoticeEnum getNoticeEnum(String code){
        return map.get(code);
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
