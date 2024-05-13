package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/5/11 10:08
 */
@Data
@TableName("datasource_reset_password_his")
public class DatasourceResetPasswordHis {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long propId;
    private String username;
    private String host;
    private String oldPassword;
    private String newPassword;
    private Date createTime;


    @TableField(exist = false)
    private String groupName;
    @TableField(exist = false)
    private String datasourceName;
    @TableField(exist = false)
    private Long computerRoomId;
    @TableField(exist = false)
    private String computerRoomName;
    @TableField(exist = false)
    private String ip;
}
