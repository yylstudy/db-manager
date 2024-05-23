package com.linkcircle.dbmanager.common;

import java.sql.Connection;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/21 16:53
 */

public interface MysqlUserGrantService {
    void grantPrivileges(Connection connection, String username, String host, String password, String db,boolean createUser);
    void updatePassword(Connection connection, String username, String host, String password,String passwordColumn);
}
