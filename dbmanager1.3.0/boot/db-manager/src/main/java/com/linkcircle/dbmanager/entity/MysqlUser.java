package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/2 16:05
 */
@Data
@TableName("mysql_user")
public class MysqlUser implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long propId;
    private String username;
    @TableField(exist = false)
    private String host;
    private String password;
    @TableField(exist = false)
    private String db;
    @TableField(exist = false)
    private String ip;
    @TableField(exist = false)
    private Integer port;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long groupId;

}
