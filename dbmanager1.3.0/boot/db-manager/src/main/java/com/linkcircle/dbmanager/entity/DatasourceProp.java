package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.linkcircle.boot.common.aspect.annotation.Dict;
import com.linkcircle.dbmanager.common.DbUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("datasource_prop")
public class DatasourceProp {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long computerRoomId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long groupId;
    private String ip;
    private Integer port;
    private String user;
    private String password;
    private Date createTime;
    /**
     * 是否k8s 0不是，1是
     */
    @Dict( dicCode = "is_k8s")
    private String isk8s;
    @TableField(exist = false)
    private List<DbUser> initUser = new ArrayList<>();
    @TableField(exist = false)
    private String encryptPwd;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    private String computerRoomName;

    private Integer sshPort;
    private String sshUser;
    private String sshPassword;
    @TableField(exist = false)
    private String encryptSshPassword;
    /**
     * 是否是sudo用户 0否，1是
     */
    private boolean sudoUser;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long k8sConfigId;
    private String namespace;
    private String podName;
    @Dict(dicCode = "enable_remote_store")
    private String enableRemoteStore;
    /**
     * mysql版本
     */
    private String mysqlVersion;

}
