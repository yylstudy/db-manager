package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.linkcircle.boot.common.aspect.annotation.Dict;
import com.linkcircle.dbmanager.typehandler.StringListToStringTypeHandler;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName(value = "backup_config",autoResultMap = true)
public class BackupConfig {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long propId;
    private String backupPath;
    private String mysqlCnf;
    private Integer dayBeforeFull;
    private Integer keepDays;
    private Date createTime;
    /**
     * 状态 0 未校验 1 校验成功 2校验失败
     */
    @Dict(dicCode = "install_software_status")
    private Integer status;
    /**
     * 还原tar文件绝对路径
     */
    @TableField(exist = false)
    private String recoverTarFile;
    /**
     * 是否本地文件
     */
    @TableField(exist = false)
    private String islocalhostFile;
    /**
     * 备份的数据库
     */
    @TableField(typeHandler = StringListToStringTypeHandler.class)
    private List<String> backupDatabases;
    @TableField(exist = false)
    private String datasourceName;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    private String computerRoomName;
    @TableField(exist = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long computerRoomId;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long businessId;
    @TableField(exist = false)
    private String mysqlSshHost;
    @TableField(exist = false)
    private Integer mysqlSshPort;
    @TableField(exist = false)
    private String mysqlSshUser;
    @TableField(exist = false)
    private String mysqlSshPassword;
}
