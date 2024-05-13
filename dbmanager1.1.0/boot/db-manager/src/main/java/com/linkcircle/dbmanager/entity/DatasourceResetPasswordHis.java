package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

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
    @Excel(name="用户名",orderNum = "5",width = 15)
    private String username;
    @Excel(name="hosts",orderNum = "6",width = 15)
    private String host;
    private String oldPassword;
    private String newPassword;
    @Excel(name="创建时间",orderNum = "7",width = 25,format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    @TableField(exist = false)
    @Excel(name="数据源组",orderNum = "2",width = 20)
    private String groupName;
    @TableField(exist = false)
    @Excel(name="业务名称",orderNum = "4",width = 15)
    private String datasourceName;
    @TableField(exist = false)
    private Long computerRoomId;
    @TableField(exist = false)
    @Excel(name="机房",orderNum = "1")
    private String computerRoomName;
    @TableField(exist = false)
    private String ip;
    @TableField(exist = false)
    @Excel(name="业务名称",orderNum = "3",width = 30)
    private String businessName;
    @TableField(exist = false)
    private String queryDateStart;
    @TableField(exist = false)
    private String queryDateEnd;
}
