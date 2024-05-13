package com.linkcircle.dbmanager;

import com.linkcircle.dbmanager.config.DbEscapeConfig;
import com.linkcircle.dbmanager.thread.JobScheduleHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 09:06
 **/
@SpringBootApplication
@MapperScan("com.linkcircle.dbmanager.mapper")
@EnableCaching
@EnableConfigurationProperties(DbEscapeConfig.class)
@EnableScheduling
public class DbManagerStart {
    public static void main(String[] args) throws Exception{
        SpringApplication.run(DbManagerStart.class, args);
        JobScheduleHelper.getInstance().start();
    }
}
