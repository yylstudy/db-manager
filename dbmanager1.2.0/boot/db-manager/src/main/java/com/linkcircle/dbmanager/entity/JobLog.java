package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.linkcircle.boot.common.aspect.annotation.Dict;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("job_log")
public class JobLog {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long jobId;
    private Date startDate;
    private Date endDate;
    @TableField(exist = false)
    private String name;
    /**
     * 执行状态，0：未调用 1：调用失败 2：成功
     */
    @Dict(dicCode="handle_code")
    private Integer handleCode;
    private String handleMsg;
    private Date createTime;
    private String executeParam;

    @TableField(exist = false)
    private String realname;
    @TableField(exist = false)
    private String userId;
    @TableField(exist = false)
    private Long businessId;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    @Dict(dicCode="task_type")
    private String taskType;
    private String ibdfrmPath;
    @TableField(exist = false)
    private Long computerRoomId;
    @TableField(exist = false)
    private String computerRoomName;
    @TableField(exist = false)
    private String configName;
    @TableField(exist = false)
    private String datasourceName;
    @TableField(exist = false)
    private Long configId;
    @TableField(exist = false)
    private String mysqlSshHost;
    @TableField(exist = false)
    private String queryDateStart;
}
