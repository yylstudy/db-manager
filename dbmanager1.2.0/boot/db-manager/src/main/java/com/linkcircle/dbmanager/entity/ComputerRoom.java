package com.linkcircle.dbmanager.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:30
 **/
@Data
@TableName("computer_room")
public class ComputerRoom {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;
    private String name;
}
