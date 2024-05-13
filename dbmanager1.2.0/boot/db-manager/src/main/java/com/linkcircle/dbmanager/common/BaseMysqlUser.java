package com.linkcircle.dbmanager.common;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/2 16:05
 */
@Data
@TableName("mysql_user")
public class BaseMysqlUser implements Serializable {
    private Long propId;
    private String username;
    private String hosts;
}
