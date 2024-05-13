package com.linkcircle.dbmanager.util;

import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.common.LinkcircleConsumer;
import com.linkcircle.dbmanager.common.LinkcircleFunction;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/17 11:31
 */
@Slf4j
public class JdbcUtil {
    static{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    public static boolean connectionMysql(String url,String username,String password){
        try(Connection connection = JdbcUtil.getConnection(url,username,password);){
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public static Connection getConnection(String url,String username,String password) throws Exception{
        Connection dbConn= DriverManager.getConnection(url,username,password);
        return dbConn;
    }

    public static <T> List<T> queryByList(Connection connection, String sql
            , LinkcircleConsumer<PreparedStatement> preparedStatementConsumer, LinkcircleFunction<ResultSet,T> function) {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try{
            ps =  connection.prepareStatement(sql);
            if(preparedStatementConsumer!=null){
                preparedStatementConsumer.accept(ps);
            }
            resultSet = ps.executeQuery();
            List<T> list = new ArrayList<>();
            while (resultSet.next()){
                T t = function.apply(resultSet);
                list.add(t);
            }
            return list;
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            closeStatement(ps);
            closeResultSet(resultSet);
        }
    }

    public static void  execute(Connection connection, String sql
            , LinkcircleConsumer<PreparedStatement> preparedStatementConsumer) {
        PreparedStatement ps = null;
        try{
            ps =  connection.prepareStatement(sql);
            if(preparedStatementConsumer!=null){
                preparedStatementConsumer.accept(ps);
            }
            String url = connection.getMetaData().getURL();
            String ip = url.split(":")[2].substring(2);
            log.info("datasource ip:{},{}",ip,ps.toString());
            ps.execute();
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            closeStatement(ps);
        }
    }

    private static void closeStatement(PreparedStatement ps){
        try{
            if(ps!=null){
                ps.close();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    private static void closeResultSet(ResultSet resultSet){
        try{
            if(resultSet!=null){
                resultSet.close();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
