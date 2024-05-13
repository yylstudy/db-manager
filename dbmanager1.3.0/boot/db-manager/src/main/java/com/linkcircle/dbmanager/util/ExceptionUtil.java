package com.linkcircle.dbmanager.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/28 16:46
 */
public class ExceptionUtil {
    public static String getMessage(Exception e){
        if(StringUtils.isNotBlank(e.getMessage())){
            return e.getMessage();
        }
        return e.toString();
    }
}
