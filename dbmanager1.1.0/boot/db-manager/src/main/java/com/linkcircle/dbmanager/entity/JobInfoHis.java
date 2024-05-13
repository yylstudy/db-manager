package com.linkcircle.dbmanager.entity;

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
@TableName("job_info_his")
public class JobInfoHis {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
    private String jobCron;
    @Dict(dicCode="task_type")
    private String taskType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long configId;
    @TableField(exist = false)
    private String businessName;
    @TableField(exist = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    @TableField(exist = false)
    private String configName;
    private Long triggerLastTime;
    private Long triggerNextTime;
    private String param;
    private Date createTime;
    @Dict(dicCode="trigger_status")
    private Integer triggerStatus;
    @TableField(exist = false)
    private String realname;
    @TableField(exist = false)
    private String userId;
    private Date delTime;

    @TableField(exist = false)
    private String datasourceName;
}
