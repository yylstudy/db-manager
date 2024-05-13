package com.linkcircle.dbmanager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.boot.common.system.vo.LoginUser;
import com.linkcircle.boot.service.CommonService;
import com.linkcircle.dbmanager.common.*;
import com.linkcircle.dbmanager.config.ExcludeNamespacesConfig;
import com.linkcircle.dbmanager.config.K8sClientManager;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.DatasourcePropHisMapper;
import com.linkcircle.dbmanager.mapper.DatasourcePropMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.ExcelUtil;
import com.linkcircle.dbmanager.util.JdbcUtil;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import com.linkcircle.system.service.ISysLogService;
import com.linkcircle.system.service.ISysUserService;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
@Slf4j
public class DatasourcePropServiceImpl extends ServiceImpl<DatasourcePropMapper, DatasourceProp> implements DatasourcePropService {
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysLogService sysLogService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private DatasourcePropHisMapper datasourcePropHisMapper;
    @Autowired
    private DatasourceGroupUserService datasourceGroupUserService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private DatasourceResetPasswordHisService datasourceResetPasswordHisService;
    @Autowired
    private ExcludeNamespacesConfig excludeNamespacesConfig;
    @Override
    public Result<?> userList(Long id) {
        DatasourceProp prop = this.getById(id);
        prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
        try(Connection connection = getConnection(prop)){
            List<DbUser> list = getUser(connection,prop,false,null);
            return Result.OK(list);
        }catch (Exception e){
            log.error("获取用户列表失败",e);
            return Result.error("获取用户列表失败"+e.toString());
        }
    }

    @Override
    public Result<?> addGroupUser(GroupUserDto groupUserDto) {
        Set<String> dbs = getDbs(groupUserDto.getDbs());
        Set<String> hosts = getHosts(groupUserDto.getHosts());
        DatasourceGroupUser dbGroupUser = datasourceGroupUserService.findByGroupIdAndUser(groupUserDto.getGroupId(),groupUserDto.getUsername());
        if(dbGroupUser!=null){
            return Result.error("组用户已存在");
        }
        String newPwd = PwdUtil.definedPWDRoles(10,10);
        String encryptPwd = PwdUtil.encryptPwd(newPwd);
        List<DatasourceProp> datasourceProps = this.findByGroupId(groupUserDto.getGroupId());
        for (DatasourceProp datasourceProp : datasourceProps) {
            MysqlUser mysqlUser = mysqlUserService.getMysqlUser(datasourceProp.getId(),groupUserDto.getUsername());
            if(mysqlUser!=null){
                return Result.error("数据库："+datasourceProp.getIp()+"用户已存在");
            }
            checkGrantPrivileges(datasourceProp);
            checkCreateUser(datasourceProp,groupUserDto.getUsername());
        }
        List<MysqlUser> mysqlUserList = new ArrayList<>();
        List<DatasourceResetPasswordHis> hisList = new ArrayList<>();
        for (DatasourceProp prop : datasourceProps) {
            try(Connection connection = getConnection(prop)){
                for(String host:hosts){
                    //这里加上IF NOT EXISTS 是防止有的双主数据库也会同步mysql库，那么用户就会创建失败，mysql5.6不支持CREATE USER  IF NOT EXISTS
//                    String sql = "  CREATE USER  IF NOT EXISTS ?@? IDENTIFIED BY ? ";
//                    JdbcUtil.execute(connection,sql,ps->{
//                        ps.setString(1,groupUserDto.getUsername());
//                        ps.setString(2,host);
//                        ps.setString(3,newPwd);
//                    });
                    for(String db:dbs){
                        grantPrivileges(connection,groupUserDto.getUsername(),host,newPwd,db);
                    }
                }
                flushPrivileges(connection);
                MysqlUser mysqlUser = new MysqlUser();
                mysqlUser.setUsername(groupUserDto.getUsername());
                mysqlUser.setPropId(prop.getId());
                mysqlUser.setPassword(encryptPwd);
                mysqlUserList.add(mysqlUser);
                DatasourceResetPasswordHis his = toPwdHis(groupUserDto.getGroupId(),prop.getId(),null,
                        PwdUtil.encryptPwd(newPwd),groupUserDto.getUsername(),groupUserDto.getHosts());
                hisList.add(his);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        transactionTemplate.execute(status->{
            DatasourceGroupUser datasourceGroupUser = new DatasourceGroupUser();
            datasourceGroupUser.setUsername(groupUserDto.getUsername());
            datasourceGroupUser.setGroupId(groupUserDto.getGroupId());
            datasourceGroupUser.setPassword(PwdUtil.encryptPwd(newPwd));
            datasourceGroupUserService.save(datasourceGroupUser);
            mysqlUserService.saveBatch(mysqlUserList);
            datasourceResetPasswordHisService.saveBatch(hisList);
            return null;
        });
        return Result.OK();
    }

    private String getPwdColumn(Connection connection){
        List<String> versions = JdbcUtil.queryByList(connection,"select version()",null,rs->rs.getString(1));
        if(versions.isEmpty()){
            throw new BusinessException("查询数据库版本号失败");
        }
        String version = versions.get(0);
        String passwordColumn = "authentication_string";
        if(version.startsWith("5.6")){
            passwordColumn = "password";
        }
        return passwordColumn;
    }


    private List<DbUser> getUser(Connection connection, DatasourceProp prop, boolean queryLocal, String user){
        String passwordColumn = getPwdColumn(connection);
        String sql = "SELECT host,user,"+passwordColumn+",select_priv FROM mysql.user where user is not null and  user not in('','mysql.session','mysql.sys','root',?) ";
        if(StringUtils.isNotBlank(user)){
            sql+=" and user=?";
        }
        Map<String,DatasourceGroupUser> map = new HashMap();
        Map<String,MysqlUser> mysqlUserMap = new HashMap();
        List<DbUser> list = JdbcUtil.queryByList(connection,sql,
            ps->{
                ps.setString(1,prop.getUser());
                if(StringUtils.isNotBlank(user)){
                    ps.setString(2,user);
                }
            },rs->{
                    String host = rs.getString(1);
                    String username = rs.getString(2);
                    String password = rs.getString(3);
                    String selectPriv = rs.getString(4);
                    DbUser dbUser = new DbUser();
                    dbUser.setHost(host);
                    dbUser.setUsername(username);
                    dbUser.setPassword(password);
                    dbUser.setSelectPriv(selectPriv);
                    dbUser.setIp(prop.getIp());
                    if(queryLocal){
                        if(prop.getGroupId()!=null){
                            MysqlUser mysqlUser = mysqlUserMap.computeIfAbsent(prop.getId()+username,key->mysqlUserService
                                    .getMysqlUser(prop.getId(),username));
                            if(mysqlUser!=null){
                                DatasourceGroupUser datasourceGroupUser = map.computeIfAbsent(prop.getGroupId()+username,key->datasourceGroupUserService
                                        .findByGroupIdAndUser(prop.getGroupId(),username));
                                if(datasourceGroupUser!=null){
                                    dbUser.setExistsLocal(true);
                                    if(datasourceGroupUser.getPassword().equals(mysqlUser.getPassword())){
                                        dbUser.setPasswordGroupMatch(true);
                                    }
                                    if(password.equals(PwdUtil.getMySQLPassword(PwdUtil.decryptPwd(mysqlUser.getPassword())))){
                                        dbUser.setPasswordMatch(true);
                                    }
                                }
                            }
                        }
                    }
                    return dbUser;
        });
        return list;
    }


    private Set<String> getDbs(String db){
        Set<String> dbs = new HashSet<>();
        for(String str:db.split(",")){
            if(StringUtils.isNotBlank(str.trim())){
                dbs.add(str.trim());
            }
        }
        if(dbs.size()>1&&dbs.contains("*")){
            throw new BusinessException("db配置错误，存在*和多个db");
        }
        return dbs;
    }
    private Set<String> getHosts(String host){
        Set<String> hosts = new HashSet<>();
        for(String str:host.split(",")){
            if(StringUtils.isNotBlank(str.trim())){
                hosts.add(str.trim());
            }
        }
        if(hosts.size()>1&&hosts.contains("%")){
            throw new BusinessException("host配置错误，存在%和多个host");
        }
        return hosts;
    }

    @Override
    public Result<?> grantGroupPrivileges(GroupUserDto groupUserDto) {
        Set<String> newDbs = getDbs(groupUserDto.getDbs());
        Set<String> newHosts = getHosts(groupUserDto.getHosts());
        List<DatasourceProp> props = this.findByGroupId(groupUserDto.getGroupId());
        for (DatasourceProp prop : props) {
            checkGrantPrivileges(prop);
        }
        List<DbUser> dbUsers = getGroupUser(groupUserDto.getGroupId(),groupUserDto.getUsername(),false);
        DatasourceGroupUser datasourceGroupUser =  datasourceGroupUserService.findByGroupIdAndUser(groupUserDto.getGroupId(),groupUserDto.getUsername());
        String password = PwdUtil.decryptPwd(datasourceGroupUser.getPassword());
        List<MysqlUser> addList = new ArrayList<>();
        for (DatasourceProp prop : props) {
            try(Connection connection = getConnection(prop)){
                //删除旧权限
                for(DbUser dbUser:dbUsers){
                    if(prop.getIp().equals(dbUser.getIp())){
                        if(newHosts.contains(dbUser.getHost())){
                            for (String s : dbUser.getDb().split(",")) {
                                revokePrivileges(connection,dbUser.getUsername(),dbUser.getHost(),"*");
                                revokePrivileges(connection,dbUser.getUsername(),dbUser.getHost(),s);
                            }
                        }else{
                            dropUser(connection,dbUser.getUsername(),dbUser.getHost());
                        }
                    }
                }
                MysqlUser dbMysqlUser = mysqlUserService.getMysqlUser(prop.getId(),groupUserDto.getUsername());
                if(dbMysqlUser==null){
                    MysqlUser mysqlUser = new MysqlUser();
                    mysqlUser.setPropId(prop.getId());
                    mysqlUser.setPassword(datasourceGroupUser.getPassword());
                    mysqlUser.setUsername(groupUserDto.getUsername());
                    addList.add(mysqlUser);
                }
                //增加新权限
                for(String newDb:newDbs){
                    for(String newHost:newHosts){
                        grantPrivileges(connection,groupUserDto.getUsername(),newHost,password,newDb);
                    }
                }
                flushPrivileges(connection);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        mysqlUserService.saveBatch(addList);
        return Result.OK();
    }

    private void checkGrantPrivileges(DatasourceProp prop){
        prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
        try(Connection connection = getConnection(prop)){
            List list = JdbcUtil.queryByList(connection,"SELECT grant_priv,super_priv FROM mysql.user where user =?",
                    ps->ps.setString(1,prop.getUser()),rs->{
                        String grantPriv = rs.getString(1);
                        String superPriv = rs.getString(2);
                        if(!CommonConstant.MYSQL_PRIV_Y.equals(grantPriv)||!CommonConstant.MYSQL_PRIV_Y.equals(superPriv)){
                            throw new BusinessException("数据源："+prop.getIp()+"用户："+prop.getUser()+"没有授权权限");
                        }
                        return "success";
                    });
            if(list.size()==0){
                throw new BusinessException("数据源："+prop.getIp()+"不存在用户："+prop.getUser());
            }
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void checkCreateUser(DatasourceProp prop,String username){
        try(Connection connection = getConnection(prop)){
            JdbcUtil.queryByList(connection,"SELECT create_user_priv,grant_priv FROM mysql.user where user =?",
                    ps->ps.setString(1,prop.getUser()),rs->{
                        String createUserPriv = rs.getString(1);
                        String grantPriv = rs.getString(2);
                        if(!CommonConstant.MYSQL_PRIV_Y.equals(createUserPriv)||!CommonConstant.MYSQL_PRIV_Y.equals(grantPriv)){
                            throw new BusinessException("数据源："+prop.getIp()+"用户："+prop.getUser()+"没有创建用户、授权权限");
                        }
                        return null;
                    });
            String selectSql = "  select count(1) from mysql.user where user=?";
            List<Long> list1 = JdbcUtil.queryByList(connection,selectSql,ps->ps.setString(1,username),rs->rs.getLong(1));
            Long total = list1.get(0);
            if(total>0){
                throw new BusinessException("数据库："+prop.getIp()+"用户已存在");
            }
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void revokePrivileges(Connection connection,String username,String host,String db){
        if(!"*".equals(db)){
            db = "`"+db+"`";
        }
        String revokeAllSql = "revoke all privileges on "+db+".* from ?@?";
        try{
            JdbcUtil.execute(connection,revokeAllSql,ps->{
                ps.setString(1,username);
                ps.setString(2,host);
            });
        }catch (Exception e){
            if(e.getCause() instanceof SQLSyntaxErrorException){
                SQLSyntaxErrorException sqlSyntaxErrorException = (SQLSyntaxErrorException)e.getCause();
                //双机房mysql库同步的时候，重复执行去除权限会报这个错误
                if(sqlSyntaxErrorException.getMessage().contains("There is no such grant defined for user")){
                    log.warn("revoke all privileges on"+db+".* from"+"'"+username+"'@'"+host+"' failure");
                }else{
                    throw e;
                }
            }
        }

    }

    private void grantPrivileges(Connection connection,String username,String host,String password,String db){
        if(!"*".equals(db)){
            db = "`"+db+"`";
        }
        String grantAllSql = "grant all privileges on "+db+".* to ?@? identified by ?";
        JdbcUtil.execute(connection,grantAllSql,ps->{
            ps.setString(1,username);
            ps.setString(2,host);
            ps.setString(3,password);
        });
    }

    @Override
    public Result<?> deleteGroupUser(GroupUserDto groupUserDto) {
        Long groupId = groupUserDto.getGroupId();
        String username = groupUserDto.getUsername();
        List<DatasourceProp> props = this.findByGroupId(groupId);
        for (DatasourceProp prop : props) {
            MysqlUser mysqlUser = mysqlUserService.getMysqlUser(prop.getId(),username);
            if(mysqlUser!=null){
                LambdaQueryWrapper<ClearDataConfig> configLambdaQueryWrapper = Wrappers.lambdaQuery();
                configLambdaQueryWrapper.eq(ClearDataConfig::getMysqlUserId,mysqlUser.getId());
                int count = clearDataConfigService.count(configLambdaQueryWrapper);
                if(count!=0){
                    return Result.error("删除失败，存在关联此用户的清理规则");
                }
            }
        }
        for (DatasourceProp prop : props) {
            prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
            try(Connection connection = getConnection(prop)){
                List<DbUser> dbUsers = getUser(connection,prop,false,username);
                for (DbUser dbUser : dbUsers) {
                    dropUser(connection,dbUser.getUsername(),dbUser.getHost());
                }
            }catch (Exception e){
                log.error("删除用户失败",e);
                return Result.error("删除用户失败"+e.toString());
            }
        }
        transactionTemplate.execute(status->{
            for (DatasourceProp prop : props) {
                LambdaQueryWrapper<MysqlUser> wrapper = Wrappers.lambdaQuery();
                wrapper.eq(MysqlUser::getPropId,prop.getId());
                wrapper.eq(MysqlUser::getUsername,username);
                mysqlUserService.remove(wrapper);
            }
            LambdaQueryWrapper<DatasourceGroupUser> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(DatasourceGroupUser::getGroupId,groupId);
            wrapper.eq(DatasourceGroupUser::getUsername,username);
            datasourceGroupUserService.remove(wrapper);
           return null;
        });
        return Result.OK();
    }


    private void dropUser(Connection connection,String username,String host){
        //mysql5.6 不支持drop user if exists
        String sql = " drop user  ?@?;";
        try{
            JdbcUtil.execute(connection,sql,ps->{
                ps.setString(1,username);
                ps.setString(2,host);
            });
        }catch (RuntimeException e){
            Throwable cause = e.getCause();
            if(cause!=null){
                String message = cause.getMessage();
                if(StringUtils.isNotBlank(message)&&message.contains("DROP USER failed")){
                    log.warn("DROP USER {}%{} failed",username,host,e);
                    return;
                }
            }
            throw e;
        }

    }

    private Connection getConnection(DatasourceProp prop){
        try{
            String url = "jdbc:mysql://"+prop.getIp()+":"+prop.getPort();
            return JdbcUtil.getConnection(url,prop.getUser(),prop.getPassword());
        }catch (Exception e){
            log.error("数据库配置错误",e);
            throw new BusinessException("数据库配置错误");
        }
    }

    @Override
    public Result<?> getDatabase(MysqlUser mysqlUser) {
        DatasourceProp prop = this.getById(mysqlUser.getPropId());
        prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
        try(Connection connection = getConnection(prop)){
            String sql = "select select_priv from mysql.user where user=? and host=?";
            List<String> selectPrivList = JdbcUtil.queryByList(connection,sql,ps->{
                ps.setString(1,mysqlUser.getUsername());
                ps.setString(2,mysqlUser.getHost());
            },rs->rs.getString(1));
            String selectPriv = selectPrivList.get(0);
            if(CommonConstant.MYSQL_PRIV_Y.equals(selectPriv)){
                return Result.OK("*");
            }
            List<MySqlDb> dbList = getDb(connection,mysqlUser.getUsername(),mysqlUser.getHost());
            String allSchemaSql = "select schema_name from information_schema.SCHEMATA";
            List<String> allDbList = JdbcUtil.queryByList(connection,allSchemaSql,null,rs->rs.getString(1));
            if(dbList.size()==allDbList.size()){
                return Result.OK("*");
            }
            String dbs = dbList.stream().map(MySqlDb::getDb).collect(Collectors.joining(","));
            return Result.OK(dbs);
        }catch (Exception e){
            log.error("获取db失败",e);
            return Result.error("获取db失败："+e.toString());
        }
    }

    @Override
    public List<DbUser> getGroupUser(Long groupId,String username,boolean queryLocal) {
        List<DbUser> allList = new ArrayList<>();
        List<DatasourceProp> datasourceProps = this.findByGroupId(groupId);
        for(DatasourceProp prop:datasourceProps){
            prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
            try(Connection connection = getConnection(prop)){
                List<DbUser> dbUsers = getUser(connection,prop,queryLocal,username);
                for (DbUser dbUser : dbUsers) {
                    dbUser.setIp(prop.getIp());
                    dbUser.setPropId(prop.getId());
                    String selectPriv = dbUser.getSelectPriv();
                    //拥有全局权限，db设置为*
                    if(CommonConstant.MYSQL_PRIV_Y.equals(selectPriv)){
                        dbUser.setDb("*");
                        allList.add(dbUser);
                        continue;
                    }else{
                        List<MySqlDb> dbList = getDb(connection,dbUser.getUsername(),dbUser.getHost());
                        String allSchemaSql = "select schema_name from information_schema.SCHEMATA";
                        List<String> allDbList = JdbcUtil.queryByList(connection,allSchemaSql,null,rs->rs.getString(1));
                        if(dbList.size()==allDbList.size()){
                            dbUser.setDb("*");
                        }else{
                            dbUser.setDb(dbList.stream().map(MySqlDb::getDb).collect(Collectors.joining(",")));
                        }
                        allList.add(dbUser);
                    }
                }

            }catch (Exception e){
                log.error("获取db失败",e);
                throw new BusinessException("获取db失败");
            }
        }
        return allList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> resetGroupUserPassword(ResetGroupUserDto resetGroupUserDto) {
        DatasourceGroupUser datasourceGroupUser = datasourceGroupUserService.findByGroupIdAndUser(resetGroupUserDto.getGroupId(),resetGroupUserDto.getUsername());
        String realPassword = PwdUtil.decryptPwd(datasourceGroupUser.getPassword());
        List<DbUser> dbUsers = resetGroupUserDto.getDbUsers();
        for (DbUser dbUser : dbUsers) {
            DatasourceProp prop = this.getById(dbUser.getPropId());
            prop.setPassword(PwdUtil.decryptPwd(prop.getPassword()));
            try(Connection connection = getConnection(prop)){
                String passwordColumn = getPwdColumn(connection);
                for (String host : dbUser.getHost().split(",")) {
                    String sql = "update mysql.user set "+passwordColumn+" = password(?) where user=? and host=?";
                    JdbcUtil.execute(connection,sql,ps->{
                        ps.setString(1,realPassword);
                        ps.setString(2,dbUser.getUsername());
                        ps.setString(3,host);
                    });
                }
                flushPrivileges(connection);
            }catch (Exception e){
                throw new BusinessException("重置密码失败");
            }
            MysqlUser dbMysqlUser = mysqlUserService.getMysqlUser(dbUser.getPropId(),resetGroupUserDto.getUsername());
            DatasourceResetPasswordHis his = toPwdHis(resetGroupUserDto.getGroupId(),dbUser.getPropId(),
                    dbMysqlUser.getPassword(),datasourceGroupUser.getPassword(),dbUser.getUsername(),dbUser.getHost());
            datasourceResetPasswordHisService.save(his);
            dbMysqlUser.setPassword(datasourceGroupUser.getPassword());
            mysqlUserService.updateById(dbMysqlUser);
        }
        return Result.OK();
    }

    private DatasourceResetPasswordHis toPwdHis(Long groupId,Long propId,String oldPassword,String newPassword,String username,String host){
        DatasourceResetPasswordHis datasourceResetPasswordHis = new DatasourceResetPasswordHis();
        datasourceResetPasswordHis.setCreateTime(new Date());
        datasourceResetPasswordHis.setGroupId(groupId);
        datasourceResetPasswordHis.setPropId(propId);
        datasourceResetPasswordHis.setOldPassword(oldPassword);
        datasourceResetPasswordHis.setNewPassword(newPassword);
        datasourceResetPasswordHis.setUsername(username);
        datasourceResetPasswordHis.setHost(host);
        return datasourceResetPasswordHis;
    }

    private List<MySqlDb> getDb(Connection connection, String username, String host){
        String dbsql = "select db,host from mysql.db where  user=? ";
        if(StringUtils.isNotBlank(host)){
            dbsql += "and host=?";
        }
        List<MySqlDb> dbList = JdbcUtil.queryByList(connection,dbsql,ps->{
            ps.setString(1,username);
            if(StringUtils.isNotBlank(host)){
                ps.setString(2,host);
            }
        },rs->{
            MySqlDb mySqlDb = new MySqlDb();
            mySqlDb.setDb(rs.getString(1));
            mySqlDb.setHost(rs.getString(2));
            return mySqlDb;
        });
        return dbList;
    }

    private void flushPrivileges(Connection connection){
        String flushPrivilegesSql = "flush privileges;";
        JdbcUtil.execute(connection,flushPrivilegesSql,null);
    }

    @Override
    public void sendAllPasswordEmail(String emails){
        try{
            List<List<String>> lists = new ArrayList<>();
            lists.add(Arrays.asList("150","350","250","150","150","350",
//                    "250",
                    "350"));
            lists.add(Arrays.asList("机房","业务名称","数据库IP","端口","用户名",
//                    "密码明文",
                    "密码密文"));
            StringBuilder sb = new StringBuilder("全量数据库密码");
            Page<DatasourceProp> page = new Page(1,1000);
            IPage<DatasourceProp> propIPage = this.queryPage(page,new DatasourceProp());
            for(DatasourceProp prop:propIPage.getRecords()){
                List<String> list = new ArrayList<>();
                lists.add(list);
                list.add(prop.getComputerRoomName());
                list.add(prop.getBusinessName());
                list.add(prop.getIp());
                list.add(String.valueOf(prop.getPort()));
                list.add(prop.getUser());
//                list.add(PwdUtil.decryptPwd(prop.getEncryptPwd()));
                list.add(prop.getEncryptPwd());
                List<MysqlUser> mysqlUserList = mysqlUserService.getMysqlUser(prop.getId());
                for(MysqlUser mysqlUser:mysqlUserList){
                    list = new ArrayList<>();
                    lists.add(list);
                    list.add(prop.getComputerRoomName());
                    list.add(prop.getBusinessName());
                    list.add(prop.getIp());
                    list.add(String.valueOf(prop.getPort()));
                    list.add(mysqlUser.getUsername());
//                    list.add(PwdUtil.decryptPwd(mysqlUser.getPassword()));
                    list.add(mysqlUser.getPassword());
                }
            }
            ByteArrayOutputStream outputStream = ExcelUtil.createExcel(lists);
            ByteArrayResource byteArrayResource = new ByteArrayResource(outputStream.toByteArray());
            String attachmentName = MimeUtility.encodeWord("数据库密码.xlsx","utf-8","B");
            commonService.sendMail(emails,"数据库密码",sb.toString(),byteArrayResource,attachmentName);
        }catch (Exception e){
            log.error("send email error",e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<?> delete(String id) {
        DatasourceProp prop = this.getById(id);
        this.removeById(id);
        LambdaQueryWrapper<MysqlUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MysqlUser::getPropId,Long.parseLong(id));
        mysqlUserService.remove(wrapper);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String logContent = user.getUsername()+"删除了数据源："+prop.getIp();
        sysLogService.add(logContent);
        DatasourcePropHis datasourcePropHis = new DatasourcePropHis();
        BeanUtils.copyProperties(prop,datasourcePropHis);
        datasourcePropHis.setDelTime(new Date());
        datasourcePropHisMapper.insert(datasourcePropHis);
        return Result.OK();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> add(DatasourceProp prop) {
        Session session = null;
        try(Connection connection = getConnection(prop)){
            if(!CommonConstant.DATASOURCE_K8S.equals(prop.getIsk8s())){
                session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),prop.getSshUser(),prop.getSshPassword());
            }
//            List<DbUser> dbList = getUser(connection,prop,false,null);
//            List<DbUser> initUsers = prop.getInitUser();
//            if(!dbList.isEmpty()){
//                if(initUsers.isEmpty()){
//                    return Result.error("存在未初始化的用户，请先初始化");
//                }
//                for(DbUser dbUser:dbList){
//                    boolean contain = false;
//                    boolean passwordMatch = false;
//                    for(DbUser initUser:initUsers){
//                        if(dbUser.equals(initUser)){
//                            String initPassword = PwdUtil.getMySQLPassword(initUser.getPassword());
//                            passwordMatch = dbUser.getPassword().equals(initPassword);
//                            contain = true;
//                            break;
//                        }
//                    }
//                    if(!contain||!passwordMatch){
//                        return Result.error("存在未初始化的用户或初始密码配置错误的用户");
//                    }
//                }
//
//            }
            prop.setPassword(PwdUtil.encryptPwd(prop.getPassword()));
            prop.setSshPassword(PwdUtil.encryptPwd(prop.getSshPassword()));
            this.save(prop);
//            List<MysqlUser> addList = new ArrayList<>();
//            for(DbUser initUser:initUsers){
//                MysqlUser mysqlUser = new MysqlUser();
//                mysqlUser.setPassword(PwdUtil.encryptPwd(initUser.getPassword()));
//                mysqlUser.setHost(initUser.getHost());
//                mysqlUser.setUsername(initUser.getUsername());
//                mysqlUser.setPropId(prop.getId());
//                addList.add(mysqlUser);
//            }
//            mysqlUserService.saveBatch(addList);
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String logContent = user.getUsername()+"创建了数据源："+prop.getIp();
            sysLogService.add(logContent);
            return Result.OK();
        }catch (Exception e){
            log.error("addUser error",e);
            return Result.error("添加数据源失败，请联系管理员"+e.getMessage());
        }finally{
            if(session!=null){
                session.disconnect();
            }
        }

    }
    @Override
    public Result<?> edit(DatasourceProp prop) {
        DatasourceProp dbProp = this.getById(prop.getId());
        String password = prop.getPassword();
        String sshPassword = prop.getSshPassword();
        if(StringUtils.isEmpty(password)){
            password = PwdUtil.decryptPwd(dbProp.getPassword());
            prop.setPassword(password);
        }
        if(StringUtils.isEmpty(sshPassword)){
            password = PwdUtil.decryptPwd(dbProp.getSshPassword());
            prop.setSshPassword(password);
        }
        Session session = null;
        try(Connection connection = getConnection(prop)){
            if(!CommonConstant.DATASOURCE_K8S.equals(prop.getIsk8s())){
                session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),prop.getSshUser(),prop.getSshPassword());
            }
            prop.setPassword(PwdUtil.encryptPwd(prop.getPassword()));
            prop.setSshPassword(PwdUtil.encryptPwd(prop.getSshPassword()));
            this.updateById(prop);
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String logContent = user.getUsername()+"修改了数据源："+prop.getIp();
            sysLogService.add(logContent);
            return Result.OK();
        }catch (Exception e){
            log.error("addUser error",e);
            return Result.error(e.getMessage());
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

    @Override
    public Result<?> getMysqlUserByLevel() {
        List<JSONObject> list = new ArrayList<>();
        List<DatasourceProp> datasourceProps = this.list();
        for(DatasourceProp prop:datasourceProps){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("label",prop.getIp());
            jsonObject.put("value",String.valueOf(prop.getId()));
            List<JSONObject> children = new ArrayList<>();
            List<MysqlUser> mysqlUserList = mysqlUserService.getMysqlUser(prop.getId());
            for(MysqlUser mysqlUser:mysqlUserList){
                JSONObject obj = new JSONObject();
                obj.put("label",mysqlUser.getUsername());
                obj.put("value",String.valueOf(mysqlUser.getId()));
                children.add(obj);
            }
            jsonObject.put("children",children);
            list.add(jsonObject);
        }
        return Result.OK(list);
    }
    @Override
    public IPage<DatasourceProp> queryPage(Page<DatasourceProp> page, DatasourceProp datasourceProp) {
        return this.baseMapper.queryPage(page, datasourceProp);
    }


    @Override
    public Result<?> getRealDatabase(Long mysqlUserId) {
        MysqlUser mysqlUser = mysqlUserService.getById(mysqlUserId);
        DatasourceProp prop = this.getById(mysqlUser.getPropId());
        List<String> dbs = getRealDatabase(prop,mysqlUser.getUsername(),mysqlUser.getPassword());
        return Result.OK(dbs);
    }

    @Override
    public Result<?> getRealDatabaseByPropId(Long propId) {
        DatasourceProp prop = this.getById(propId);
        List<String> dbs = getRealDatabase(prop,prop.getUser(),prop.getPassword());
        return Result.OK(dbs);
    }

    private List<String> getRealDatabase(DatasourceProp prop, String username, String password) {
        String url = "jdbc:mysql://"+prop.getIp()+":"+prop.getPort();
        try(Connection connection =JdbcUtil.getConnection(url,username,
                PwdUtil.decryptPwd(password))){
            List<String> dbs = JdbcUtil.queryByList(connection,"show databases",null,rs->rs.getString(1));
            dbs = dbs.stream().filter(db->!db.equals("information_schema")&&!db.equals("performance_schema")&&!db.equals("sys")&&!db.equals("mysql"))
                    .collect(Collectors.toList());
            return dbs;
        }catch (Exception e){
            log.error("获取数据库失败",e);
            throw new BusinessException("获取数据库失败");
        }
    }

    @Override
    public List<DatasourceProp> findByGroupId(Long groupId) {
        LambdaQueryWrapper<DatasourceProp> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DatasourceProp::getGroupId,groupId);
        return this.list(wrapper);
    }

    @Override
    public Result userGroupList(Long groupId) {
        List<DatasourceProp> datasourceProps = this.findByGroupId(groupId);
        List<DbUser> allList = new ArrayList<>();
        for(DatasourceProp datasourceProp:datasourceProps){
            datasourceProp.setPassword(PwdUtil.decryptPwd(datasourceProp.getPassword()));
            try(Connection connection = getConnection(datasourceProp)){
                List<DbUser> dbList = getUser(connection,datasourceProp,true,null);
                allList.addAll(dbList);
            }catch (Exception e){
                log.error("获取用户列表失败",e);
                return Result.error("获取用户列表失败"+e.toString());
            }
        }
        List<DbUser> dbUsers =  allList.stream().collect(Collectors.groupingBy(DbUser::getUsername))
                .values().stream().map(list->{
                    boolean showGrant = true;
                    boolean showReset = true;
                    Set<String> ipSet = new HashSet<>();
                    StringBuilder remark = new StringBuilder();
                    for (DbUser dbUser : list) {
                        if(!dbUser.isExistsLocal()){
                            showReset = false;
                            remark.append("数据库:"+dbUser.getIp()+"，host:"+dbUser.getHost()+"，本地用户不存在\r\n");
                        }else{
                            if(!dbUser.isPasswordGroupMatch()){
                                showGrant = false;
                                remark.append(dbUser.getIp()+"组密码与本地密码不匹配\r\n");
                            }
                            if(!dbUser.isPasswordMatch()){
                                showGrant = false;
                                showReset = false;
                                remark.append(dbUser.getIp()+"本地密码与真实密码不匹配\r\n");
                            }
                        }
                        ipSet.add(dbUser.getIp());
                    }
                    DbUser dbUser = new DbUser();
                    String notExistsUserIp = datasourceProps.stream().filter(prop->!ipSet.contains(prop.getIp()))
                            .map(DatasourceProp::getIp).collect(Collectors.joining(","));
                    if(StringUtils.isNotBlank(notExistsUserIp)){
                        showReset = false;
                        remark.append(notExistsUserIp+"不存在此用户\r\n");
                    }
                    DatasourceGroupUser datasourceGroupUser = datasourceGroupUserService
                            .findByGroupIdAndUser(groupId,list.get(0).getUsername());
                    if(datasourceGroupUser==null){
                        showGrant = false;
                    }
                    dbUser.setRemark(remark.toString());
                    dbUser.setIp(ipSet.stream().collect(Collectors.joining(",")));
                    dbUser.setUsername(list.get(0).getUsername());
                    dbUser.setShowGrant(showGrant);
                    dbUser.setShowReset(showReset);
                    return dbUser;
                }).collect(Collectors.toList());
        return Result.OK(dbUsers);
    }

    @Override
    public Result resetGroupPwd(GroupUserDto groupUserDto) {
        String password = PwdUtil.encryptPwd(PwdUtil.definedPWDRoles(10,10));
        DatasourceGroupUser user = new DatasourceGroupUser();
        user.setPassword(password);
        user.setGroupId(groupUserDto.getGroupId());
        user.setUsername(groupUserDto.getUsername());
        LambdaQueryWrapper<DatasourceGroupUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DatasourceGroupUser::getGroupId,groupUserDto.getGroupId());
        wrapper.eq(DatasourceGroupUser::getUsername,groupUserDto.getUsername());
        datasourceGroupUserService.saveOrUpdate(user,wrapper);
        return Result.OK();
    }

    @Override
    public Result<?> getNamespace(Long k8sConfigId) {
        CoreV1Api api = K8sClientManager.getCoreV1Api(k8sConfigId);
        try {
            V1NamespaceList v1NamespaceList = api.listNamespace(null,null,null,null,null,null,
                    null,null,null,null);
            List<String> namespaceList = v1NamespaceList.getItems().stream().map(v1Namespace ->
                    v1Namespace.getMetadata().getName()).filter(str->excludeNamespacesConfig.getExcludeNamespaces().stream()
                    .noneMatch(excludeNamespace->str.contains(excludeNamespace))).collect(Collectors.toList());
            return Result.OK(namespaceList);
        } catch (Exception e) {
            log.error("获取namespace失败",e);
            return Result.error("获取namespace失败");
        }
    }

    @Override
    public Result<?> getPod(Long k8sConfigId, String namespace) {
        CoreV1Api api = K8sClientManager.getCoreV1Api(k8sConfigId);
        try {
            V1PodList podList = api.listNamespacedPod(namespace, null, null, null, null, null, null,
                        null, null, null,false);
            List<String> namespaceList = podList.getItems().stream().map(v1Pod ->
                    v1Pod.getMetadata().getName()).filter(str->str.contains("mysql")&&!str.contains("operator")).collect(Collectors.toList());
            return Result.OK(namespaceList);
        } catch (Exception e) {
            log.error("获取pod失败",e);
            return Result.error("获取pod失败");
        }
    }
}
