package com.linkcircle.dbmanager.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/22 18:23
 */
@Data
public class GroupUserDto implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;
    private String username;
    private String hosts;
    private String dbs;
}
