package com.linkcircle.dbmanager.common;

import com.linkcircle.dbmanager.util.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/21 16:53
 */
@Service
@Slf4j
public class Mysql8UserGrantService implements MysqlUserGrantService{
    @Override
    public void grantPrivileges(Connection connection, String username, String host, String password, String db,boolean createUser) {
        if(createUser){
            String createUserSql = "create user ?@? identified by ? PASSWORD EXPIRE NEVER";
            try{
                JdbcUtil.execute(connection,createUserSql, ps->{
                    ps.setString(1,username);
                    ps.setString(2,host);
                    ps.setString(3,password);
                });
            }catch (Exception e){
                if(e.getMessage().contains("Operation CREATE USER failed for")){
                    log.error("create user ignore");
                }else{
                    throw e;
                }
            }

        }
        if(!"*".equals(db)){
            db = "`"+db+"`";
        }
        String grantAllSql = "grant all privileges on "+db+".* to ?@?";
        JdbcUtil.execute(connection,grantAllSql, ps->{
            ps.setString(1,username);
            ps.setString(2,host);
        });
    }

    @Override
    public void updatePassword(Connection connection, String username, String host, String password, String passwordColumn) {
        String sql = "ALTER USER ?@? IDENTIFIED WITH mysql_native_password BY ?";
        JdbcUtil.execute(connection,sql,ps->{
            ps.setString(1,username);
            ps.setString(2,host);
            ps.setString(3,password);
        });
    }
}
