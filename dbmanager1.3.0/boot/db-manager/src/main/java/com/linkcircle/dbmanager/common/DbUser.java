package com.linkcircle.dbmanager.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/5 10:54
 */
@Setter
@Getter
@EqualsAndHashCode(exclude = {"password","isLocalUser"})
@ToString
public class DbUser implements Serializable {
    private String host;
    private String username;
    /**
     * 是否存在本地
     */
    private boolean existsLocal;
    /**
     * 组密码和本地密码是否匹配
     */
    private boolean passwordGroupMatch;
    /**
     * 本地密码表和mysql密码是否匹配
     */
    private boolean passwordMatch;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String ip;
    private String db;
    private String remark;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long propId;
    /**
     * 是否显示授权按钮
     */
    private boolean showGrant;
    /**
     * 是否重置密码按钮
     */
    private boolean showReset;

    private String groupPassword;

    private String selectPriv;

}
