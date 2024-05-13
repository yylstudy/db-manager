package com.linkcircle.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: yang.yonglian
 * @create: 2021-07-09
 **/
@Configuration
@ComponentScan("com.linkcircle.boot")
@MapperScan("com.linkcircle.boot.mapper")
public class CoreAutoConfig {
}
