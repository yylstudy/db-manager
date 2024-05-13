package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/12 9:42
 */
@Data
@TableName("clear_table_name_rule")
public class TableNameRule implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    /**
     * 清理规则ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long clearDataConfigId;
    /**
     * 表名正则
     */
    private String tableNameRegular;
    /**
     * 是否包含时间
     */
    private Integer containTime;
    /**
     * 时间格式
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String timeRule;
    /**
     * 距当前清理开始时间
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer clearTimeStart;
    /**
     * 距当前清理结束时间
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer clearTimeEnd;

}
