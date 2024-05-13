package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.linkcircle.boot.common.aspect.annotation.Dict;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

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
    @Excel(name="执行开始时间",orderNum = "6",format = "yyyy-MM-dd HH:mm:ss",width = 20)
    private Date startDate;
    @Excel(name="执行结束时间",orderNum = "7",format = "yyyy-MM-dd HH:mm:ss",width = 20)
    private Date endDate;
    @TableField(exist = false)
    private String name;
    /**
     * 执行状态，0：未调用 1：调用失败 2：成功
     */
    @Dict(dicCode="handle_code")
    @Excel(name="执行结果",orderNum = "8",dicCode = "handle_code")
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
    @Excel(name="业务名称",orderNum = "2",width = 30)
    private String businessName;
    @TableField(exist = false)
    @Dict(dicCode="task_type")
    @Excel(name="任务类型",orderNum = "3",dicCode = "task_type",dicText="task_type")
    private String taskType;
    private String ibdfrmPath;
    @TableField(exist = false)
    private Long computerRoomId;
    @TableField(exist = false)
    @Excel(name="机房",orderNum = "1")
    private String computerRoomName;
    @TableField(exist = false)
    @Excel(name="规则名称",orderNum = "4",width = 20)
    private String configName;
    @TableField(exist = false)
    @Excel(name="数据源",orderNum = "5",width = 15)
    private String datasourceName;
    @TableField(exist = false)
    private Long configId;
    @TableField(exist = false)
    private String mysqlSshHost;
    @TableField(exist = false)
    private String queryDate;
    @TableField(exist = false)
    private String queryDateStart;
    @TableField(exist = false)
    private String queryDateEnd;
    @TableField(exist = false)
    private String proBusName;

}
