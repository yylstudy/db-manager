package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("mongodb_prop")
public class MongodbProp {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long computerRoomId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private String ip;
    private Integer port;
    private String user;
    private String password;
    @TableField(exist = false)
    private String encryptPassword;
    private Date createTime;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    private String computerRoomName;
    private Integer sshPort;
    private String sshUser;
    private String sshPassword;
    @TableField(exist = false)
    private String encryptSshPassword;
    private String backupPath;
}
