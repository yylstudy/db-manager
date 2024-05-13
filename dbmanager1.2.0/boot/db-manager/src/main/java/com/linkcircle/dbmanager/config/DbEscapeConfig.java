package com.linkcircle.dbmanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/13 15:27
 */
@ConfigurationProperties(prefix = "db.escape")
@Data
public class DbEscapeConfig {
    private Map<String,String> map;

    public String getDatabaseDir(String database){
        for(Map.Entry<String,String> entry:map.entrySet()){
            database = database.replace(entry.getKey(),entry.getValue());
        }
        return database;
    }
}
