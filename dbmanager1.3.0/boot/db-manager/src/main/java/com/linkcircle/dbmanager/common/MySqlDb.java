package com.linkcircle.dbmanager.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/21 14:06
 */
@Data
public class MySqlDb implements Serializable {
    private String host;
    private String db;
    private String datasoruceIp;
}
