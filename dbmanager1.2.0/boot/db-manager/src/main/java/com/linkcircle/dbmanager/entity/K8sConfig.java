package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("k8s_config")
public class K8sConfig {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
    @TableField(exist = false)
    private List<String> attachementIds;

    private String nfsSshIp;
    private Integer nfsSshPort;
    private String nfsSshUser;
    private String nfsSshPassword;
    private String nfsBaseDir;

}
