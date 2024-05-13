package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.linkcircle.boot.common.aspect.annotation.Dict;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("clear_data_config")
public class ClearDataConfig {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long mysqlUserId;
    private String dbDatabase;
    @Dict( dicCode = "clear_type")
    private String clearType;
    private Date createTime;
//    private Integer mysqlSshPort;
//    private String mysqlSshUser;
//    private String mysqlSshPassword;
    private String ibdFrmDir;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long computerRoomId;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long businessId;
    @TableField(exist = false)
    private List<TableNameRule> tableNameRules;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    private String computerRoomName;
    @TableField(exist = false)
    private String datasourceName;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long propId;
    @TableField(exist = false)
    private String mysqlSshHost;
}
