package com.linkcircle.dbmanager.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/24 14:56
 */

public class FileUtil {
    public static String getMaxTimeFile(String maxTimeFile,String fileName){
        if(StringUtils.isEmpty(maxTimeFile)){
            maxTimeFile = fileName;
        }else{
            long fileTime = Long.parseLong(fileName.substring(4));
            long maxIncrTime = Long.parseLong(maxTimeFile.substring(4));
            if(fileTime>maxIncrTime){
                maxTimeFile = fileName;
            }
        }
        return maxTimeFile;
    }
}
