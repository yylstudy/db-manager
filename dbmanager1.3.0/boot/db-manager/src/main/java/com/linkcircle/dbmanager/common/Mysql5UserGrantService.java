package com.linkcircle.dbmanager.common;

import com.linkcircle.dbmanager.util.JdbcUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/21 16:53
 */
@Service
public class Mysql5UserGrantService implements MysqlUserGrantService{
    @Override
    public void grantPrivileges(Connection connection, String username, String host, String password, String db,boolean createUser) {
        if(!"*".equals(db)){
            db = "`"+db+"`";
        }
        String grantAllSql = "grant all privileges on "+db+".* to ?@? identified by ?";
        JdbcUtil.execute(connection,grantAllSql, ps->{
            ps.setString(1,username);
            ps.setString(2,host);
            ps.setString(3,password);
        });
    }

    @Override
    public void updatePassword(Connection connection, String username, String host, String password, String passwordColumn) {
        String sql = "update mysql.user set "+passwordColumn+" = password(?) where user=? and host=?";
        JdbcUtil.execute(connection,sql,ps->{
            ps.setString(1,password);
            ps.setString(2,username);
            ps.setString(3,host);
        });
    }
}
