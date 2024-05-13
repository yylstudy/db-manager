package com.linkcircle.dbmanager.entity;

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
@TableName("datasource_prop_his")
public class DatasourcePropHis {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long computerRoomId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private String ip;
    private Integer port;
    private String user;
    private String password;
    private Date createTime;
    private Integer sshPort;
    private String sshUser;
    private String sshPassword;
    private Date delTime;
}
