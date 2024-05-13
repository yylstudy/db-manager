package com.linkcircle.dbmanager;

import com.linkcircle.dbmanager.config.DbEscapeConfig;
import com.linkcircle.dbmanager.job.ThreadPoolMonitor;
import com.linkcircle.dbmanager.thread.JobScheduleHelper;
import org.jeecgframework.core.util.ApplicationContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@Import(ApplicationContextUtil.class)
public class DbManagerStart {
    public static void main(String[] args) throws Exception{
        SpringApplication.run(DbManagerStart.class, args);
        JobScheduleHelper.getInstance().start();
    }
    @Autowired
    private ThreadPoolMonitor threadPoolMonitor;
    @RequestMapping("test111")
    public String test1(){
        threadPoolMonitor.reportTask();
        return "success";
    }
}
