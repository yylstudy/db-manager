package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.aspect.annotation.JRepeat;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.common.ClearTypeEnum;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.TimeRule;
import com.linkcircle.dbmanager.entity.ClearDataConfig;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.MysqlUser;
import com.linkcircle.dbmanager.entity.TableNameRule;
import com.linkcircle.dbmanager.mapper.ClearDataConfigMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.BackupServiceUtil;
import com.linkcircle.dbmanager.util.JdbcUtil;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
@Slf4j
public class ClearDataConfigServiceImpl extends ServiceImpl<ClearDataConfigMapper, ClearDataConfig> implements ClearDataConfigService {
    @Autowired
    private TableNameRuleService tableNameRuleService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Override
    public boolean testConnection(Long id) {
        ClearDataConfig clearDataConfig = this.getById(id);
        MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
        String url = getUrl(mysqlUser.getIp(),mysqlUser.getPort(),clearDataConfig.getDbDatabase());
        String pwd = PwdUtil.decryptPwd(mysqlUser.getPassword());
        boolean getConnection = JdbcUtil.connectionMysql(url,
                mysqlUser.getUsername(),pwd);
        return getConnection;
    }

    @Override
    public List<String> getClearTableName(Long id) {
        ClearDataConfig clearDataConfig = this.getById(id);
        MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
        String url = getUrl(mysqlUser.getIp(),mysqlUser.getPort(),clearDataConfig.getDbDatabase());
        String pwd = PwdUtil.decryptPwd(mysqlUser.getPassword());
        try(Connection connection = JdbcUtil.getConnection(url,
                mysqlUser.getUsername(),pwd)){
            return getClearTableName(connection,clearDataConfig);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    @JRepeat(lockTime = 30,lockKey = "cleardata_${clearDataConfig.mysqlUserId}")
    public Result clearData(ClearDataConfig clearDataConfig,boolean checkTime) {
        MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
        String url = getUrl(mysqlUser.getIp(),mysqlUser.getPort(),clearDataConfig.getDbDatabase());
        String pwd = PwdUtil.decryptPwd(mysqlUser.getPassword());
        try(Connection connection = JdbcUtil.getConnection(url,
                mysqlUser.getUsername(),pwd)){
            StringBuilder sb = new StringBuilder();
            List<String> clearTableName = getClearTableName(connection,clearDataConfig);
            if(clearTableName.isEmpty()){
                return Result.OK("清理的表名集合为空,无需清理！",null);
            }
            ClearTypeEnum clearTypeEnum = ClearTypeEnum.getEnum(clearDataConfig.getClearType());
            if(clearTypeEnum ==null){
                throw new BusinessException("无效的清理类型");
            }
            DatasourceProp prop = datasourcePropService.getById(mysqlUser.getPropId());
            BackupClearDataService backupClearDataService = BackupServiceUtil.getBackupService(prop.getIsk8s());
            Result result = backupClearDataService.backupIbdAndFrm(clearDataConfig,prop,connection,clearTableName,checkTime);
            if(!result.isSuccess()){
                return result;
            }
            String countSqlPrefix = "select count(1) from `"+clearDataConfig.getDbDatabase()+"`.";
            if(clearTypeEnum==ClearTypeEnum.DELETE){
                deleteData(clearTableName,connection,countSqlPrefix,clearDataConfig.getDbDatabase(),sb);
            }else if(clearTypeEnum==ClearTypeEnum.TRUNCATE){
                truncateData(clearTableName,connection,countSqlPrefix,clearDataConfig.getDbDatabase(),sb);
            }
            return Result.OK(sb.toString(),result.getResult());
        }catch (Exception e){
            log.error("清理数据失败",e);
            return Result.error("清理数据失败:"+e.toString());
        }
    }

    private void deleteData(List<String> clearTableName ,Connection connection,
                              String countSqlPrefix,String database,StringBuilder sb) throws Exception{
        connection.setAutoCommit(false);
        try{
            for(String tableName:clearTableName) {
                String countSql = countSqlPrefix+tableName;
                List<Long> countList = JdbcUtil.queryByList(connection,countSql,null,rs->rs.getLong(1));
                String clearSql = "delete from `"+database+"`."+tableName;
                log.info("delete sql:{}",clearSql);
                JdbcUtil.execute(connection,clearSql,null);
                sb.append("表名：").append(tableName).append(" delete 成功，清理数据量：").append(countList.get(0)).append("<br/>");
            }
            connection.commit();
        }catch (Exception e){
            log.error("delete data error",e);
            connection.rollback();
            throw e;
        }
    }
    private void truncateData(List<String> clearTableName ,Connection connection,
                              String countSqlPrefix,String database,StringBuilder sb) throws Exception{
        for(String tableName:clearTableName){
            try{
                String countSql = countSqlPrefix+tableName;
                List<Long> countList = JdbcUtil.queryByList(connection,countSql,null,rs->rs.getLong(1));
                String clearSql = " truncate `"+database+"`."+tableName;
                log.info("truncate sql:{}",clearSql);
                JdbcUtil.execute(connection,clearSql,null);
                sb.append("表名：").append(tableName).append(" truncate 成功，清理数据量：").append(countList.get(0)).append("<br/>");
            }catch (Exception e){
                log.error("clear data error",e);
                sb.append("表名：").append(tableName).append(" truncate 失败，").append("失败明细：").append(e.toString()).append("<br/>");
            }
        }
    }

    private List<String> getClearTableName(Connection connection,ClearDataConfig clearDataConfig){
        List<TableNameRule> tableNameRules = tableNameRuleService.findByClearDataConfigId(clearDataConfig.getId());
        String sql = "select table_name from information_schema.tables where table_schema=? and table_rows>0";
        List<String> tableNames = JdbcUtil.queryByList(connection,sql, ps->{
            ps.setString(1,clearDataConfig.getDbDatabase());
        },rs->rs.getString(1));
        List<String> targetTableNames = new ArrayList<>();
        for(TableNameRule tableNameRule:tableNameRules){
            String tableNameRegular = tableNameRule.getTableNameRegular();
            Pattern pattern = Pattern.compile(tableNameRegular);
            List<String> regularTableList = tableNames.stream().filter(tableName->
                    pattern.matcher(tableName).matches()).collect(Collectors.toList());
            Integer containTime = tableNameRule.getContainTime();
            if(CommonConstant.CONTAIN_TIME.equals(containTime)){
                regularTableList = regularTableList.stream().filter(tableName->{
                    String timeRuleStr = tableNameRule.getTimeRule();
                    Integer clearTimeStart = tableNameRule.getClearTimeStart();
                    Integer clearTimeEnd = tableNameRule.getClearTimeEnd();
                    return !targetTableNames.contains(tableName)&&TimeRule.isMatch(timeRuleStr,tableName,clearTimeStart,clearTimeEnd);
                }).sorted(Comparator.comparing(String::valueOf)).collect(Collectors.toList());
            }
            targetTableNames.addAll(regularTableList);
        }
        return targetTableNames;
    }


    private String getUrl(String ip,Integer port,String database){
        String url = "jdbc:mysql://"+ip+":"+port+"/"+database;
        return url;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ClearDataConfig clearDataConfig) {
        this.save(clearDataConfig);
        clearDataConfig.getTableNameRules().stream().forEach(rule->rule.setClearDataConfigId(clearDataConfig.getId()));
        tableNameRuleService.saveBatch(clearDataConfig.getTableNameRules());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ClearDataConfig clearDataConfig) {
        List<TableNameRule> dbTableNameRules = tableNameRuleService.findByClearDataConfigId(clearDataConfig.getId());
        List<TableNameRule> tableNameRules = clearDataConfig.getTableNameRules();
        tableNameRules.stream().forEach(rule->rule.setClearDataConfigId(clearDataConfig.getId()));
        List<Long> deleteIds = dbTableNameRules.stream().filter(dbRule->tableNameRules.stream().noneMatch(tableNameRule ->
                dbRule.getId().equals(tableNameRule.getId())))
                .map(TableNameRule::getId).collect(Collectors.toList());
        tableNameRuleService.removeByIds(deleteIds);
        tableNameRuleService.saveOrUpdateBatch(tableNameRules);
        this.updateById(clearDataConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        this.removeById(id);
        LambdaQueryWrapper<TableNameRule> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TableNameRule::getClearDataConfigId,id);
        tableNameRuleService.remove(wrapper);
    }

    @Override
    public IPage<ClearDataConfig> queryPage(Page<ClearDataConfig> page, ClearDataConfig clearDataConfig) {
        return this.baseMapper.queryPage(page, clearDataConfig);
    }

    @Override
    public Result testSsh(ClearDataConfig clearDataConfig) {
        Session session = null;
        try{
            MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
            DatasourceProp datasourceProp = datasourcePropService.getById(mysqlUser.getPropId());
            session = SshUtil.getSession(mysqlUser.getIp(),datasourceProp.getSshPort(),
                    datasourceProp.getSshUser(),datasourceProp.getSshPassword());
            return Result.OK("ssh连接成功",null);
        }catch (Exception e){
            log.error("ssh连接失败",e);
            return Result.error("ssh连接失败");
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

    @Override
    public Result testmysql(ClearDataConfig clearDataConfig) {
        MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
        String url = "jdbc:mysql://"+mysqlUser.getIp()+":"+mysqlUser.getPort()+"/"+clearDataConfig.getDbDatabase();
        String password = PwdUtil.decryptPwd(mysqlUser.getPassword());
        boolean getConnection = JdbcUtil.connectionMysql(url,
                mysqlUser.getUsername(),password);
        if(getConnection){
            return Result.OK("mysql连接成功",null);
        }else{
            return Result.error("mysql连接失败");
        }
    }

    @Override
    public long countByPropId(long propId) {
        return this.baseMapper.countByPropId(propId);
    }
}
