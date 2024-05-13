package com.linkcircle.dbmanager.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/24 14:48
 */
@Data
public class ResetGroupUserDto implements Serializable {
    private Long groupId;
    private String username;
    private List<DbUser> dbUsers;

}
