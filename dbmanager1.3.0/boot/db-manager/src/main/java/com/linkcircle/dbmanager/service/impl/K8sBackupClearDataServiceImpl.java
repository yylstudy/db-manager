package com.linkcircle.dbmanager.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.boot.common.util.CommonUtils;
import com.linkcircle.boot.common.util.DateUtils;
import com.linkcircle.dbmanager.common.BackupResult;
import com.linkcircle.dbmanager.common.BackupType;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.ExecResult;
import com.linkcircle.dbmanager.config.DbEscapeConfig;
import com.linkcircle.dbmanager.config.K8sClientManager;
import com.linkcircle.dbmanager.entity.BackupConfig;
import com.linkcircle.dbmanager.entity.ClearDataConfig;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.MysqlUser;
import com.linkcircle.dbmanager.service.BackupClearDataService;
import com.linkcircle.dbmanager.service.MysqlUserService;
import com.linkcircle.dbmanager.util.*;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/24 14:35
 */
@Service("k8sBackupServiceImpl")
@Slf4j
public class K8sBackupClearDataServiceImpl implements BackupClearDataService {
    @Autowired
    private DbEscapeConfig dbEscapeConfig;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Override
    public void checkSoftInstallResult(BackupConfig backupConfig, DatasourceProp prop) {
        Result result = checkSoftInstall(backupConfig,prop);
        List<Boolean> checkResultList = (List<Boolean>)result.getResult();
        StringBuilder checkResult = new StringBuilder();
        if(!checkResultList.get(0)){
            checkResult.append("pod不存在");
        }
        if(!checkResultList.get(1)){
            checkResult.append("qpress未安装");
        }
        if(!checkResultList.get(2)){
            checkResult.append("备份目录不存在、");
        }
        if(!checkResultList.get(3)){
            checkResult.append("数据库配置或database配置错误、");
        }
        String str = checkResult.toString();
        if(StringUtils.isNotBlank(str)){
            throw new BusinessException(str.substring(0,str.length()-1));
        }
    }


    @Override
    public BackupResult doBackup(BackupConfig backupConfig, DatasourceProp prop, boolean checkTime) {
        BackupResult backupResult = new BackupResult();
        ApiClient apiClient = K8sClientManager.get(prop.getK8sConfigId());
        CoreV1Api api = new CoreV1Api(apiClient);
        Exec exec = new Exec(apiClient);
        String newBackFileName = "";
        try{
            checkSoftInstallResult(backupConfig,prop);
            String basePath = backupConfig.getBackupPath();
            //备份路径以/结尾，则去除/
            if(backupConfig.getBackupPath().endsWith(CommonConstant.SEPARATOR)){
                basePath = basePath.substring(0,basePath.length()-1);
            }
            String backupPath = basePath;
            String backlogPath = backupPath+ CommonConstant.SEPARATOR +"backupLog";
            String fullPrefix = CommonConstant.FULLPREFIX;
            String incrPrefix = CommonConstant.INCRPREFIX;
            String allPrefix = CommonConstant.ALLRPREFIX;
            String successFlag = CommonConstant.SUCCESS_FLAG;
            V1Pod v1Pod;
            try{
                v1Pod = api.readNamespacedPod(prop.getPodName(), prop.getNamespace(),null);
            }catch (Exception e){
                log.error("获取pod异常",e);
                backupResult.setMsg("获取pod异常，"+ExceptionUtil.getMessage(e));
                return backupResult;
            }
            List<String> dirs = K8sExecUtil.ls(exec,v1Pod,backupPath);
            String maxTimeFile = "";
            String maxTimeFullFile = "";
            List<String> allTarFiles = new ArrayList<>();
            //增量和全量目录命名规则为 fullyyyyMMddHHmmss和incryyyyMMddHHmmss
            //找出最近的全量文件
            //找出最近的备份文件
            for(String dir:dirs){
                if(dir.contains(fullPrefix)){
                    maxTimeFullFile = FileUtil.getMaxTimeFile(maxTimeFullFile,dir);
                }
                if(dir.contains(incrPrefix)||dir.contains(fullPrefix)){
                    maxTimeFile = FileUtil.getMaxTimeFile(maxTimeFile,dir);
                }
                if(dir.contains(allPrefix)){
                    allTarFiles.add(dir);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("rm -rf "+backlogPath+" && xtrabackup");
            Date date = new Date();
            String time = DateUtils.formatDateForYyyymmddhhmmss(new Date());
            String pwd = PwdUtil.decryptPwd(prop.getPassword());
            sb.append(" --backup --compress --compress-threads=1 ")
                    .append(" --host=").append(CommonConstant.K8S_DEFAULT_MYSQL_IP)
                    .append(" --port=").append(CommonConstant.K8S_DEFAULT_MYSQL_PORT)
                    .append(" --user=" ).append(prop.getUser())
//                        .append(" --password=").append(backupConfig.getMysqlPassword())
                    .append(" --password=\"").append(pwd).append("\"")
                    .append(" --no-timestamp  ");
            if(CollectionUtils.isNotEmpty(backupConfig.getBackupDatabases())){
                Set<String> set = new HashSet<>(backupConfig.getBackupDatabases());
                set.addAll(CommonConstant.DEFAULT_DATABASES);
                String realDb =  set.stream()
                        .map(dbEscapeConfig::getDatabaseDir).collect(Collectors.joining(" "));
                sb.append(" --databases=\"").append(realDb).append("\" ");
            }
            //不存在全量文件，全备
            if(StringUtils.isEmpty(maxTimeFullFile)){
                sb.append(" --target-dir=").append(backupPath+CommonConstant.SEPARATOR+fullPrefix+time)
                        .append(" > ").append(backlogPath).append(" 2>&1 && cat "+backlogPath);
                backupResult.setBackupType(BackupType.FULL);
                maxTimeFullFile = fullPrefix+time;
            }else{
                //增量备份
                backupResult.setBackupType(BackupType.INCR);
                sb.append(" --target-dir=").append(backupPath+ CommonConstant.SEPARATOR+incrPrefix+time)
                        .append(" --incremental-basedir=").append(backupPath+CommonConstant.SEPARATOR+maxTimeFile)
                        .append(" > ").append(backlogPath).append(" 2>&1 && cat "+backlogPath);

            }
            newBackFileName = backupPath+CommonConstant.SEPARATOR+(backupResult.getBackupType().getCode())+time;
            String backupCommand = sb.toString();
            log.info("backupCommand:{}",backupCommand);
            int hour = LocalDateTime.now().getHour();
            if(checkTime&&hour>=6&&hour<=21){
                String currentDate = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT);
                backupResult.setMsg("java执行备份异常：<br/>备份命令不在允许的时间段内，请检查配置或线程池是否有残余任务，当前时间为："+currentDate);
                return backupResult;
            }
            ExecResult execResult = K8sExecUtil.doExec(exec,v1Pod,new String[]{"sh","-c",backupCommand});
            String backupLog = execResult.getMessage();
            if(CommonConstant.EXEC_SUCCESS.equals(execResult.getCode())
                    &&StringUtils.countMatches(backupLog,successFlag)==1){
                backupResult.setSuccess(true);
                backupResult.setBackupDir(newBackFileName);
                backupResult.setMsg(backupLog);
                String lastFullTimeStr = maxTimeFullFile.substring(4);
                Date lastFullTime = DateUtil.parse(lastFullTimeStr, DatePattern.PURE_DATETIME_PATTERN);
                long differDay = DateUtil.betweenDay(lastFullTime , date,true);
                //超过全量备份周期
                if((differDay>=backupConfig.getDayBeforeFull()-1)){
                    tarOldFullFile(exec,v1Pod,backupPath,lastFullTimeStr,backupResult);
                }
                String deleteFiles = deleteOldAllTarFiles(exec,v1Pod,backupPath,allTarFiles,backupConfig.getKeepDays());
                backupResult.setDeleteTarFile(deleteFiles);

            }else{
                log.error("备份异常，结果："+execResult);
                //有可能还在写入，睡眠一分钟，等待写入完成
                Thread.sleep(60000);
                K8sExecUtil.rmDir(exec,v1Pod,newBackFileName);
                backupResult.setMsg("java执行备份命令异常：<br/>"+backupLog);
                return backupResult;
            }

        }catch (Exception e){
            try{
                if(StringUtils.isNotBlank(newBackFileName)){
                    K8sExecUtil.rmDir(exec,prop.getNamespace(),prop.getPodName(),newBackFileName);
                }
                log.error("备份失败",e);
                backupResult.setMsg("备份过程中出现异常："+ ExceptionUtil.getMessage(e));
            }catch (Exception ee){
                log.error("ee error",ee);
            }
        }
        return backupResult;
    }


    @Override
    public Result backupIbdAndFrm(ClearDataConfig clearDataConfig, DatasourceProp prop, Connection connection, List<String> clearTableName, boolean checkTime) {
        try{
            MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
            String ibdFrmDir = clearDataConfig.getIbdFrmDir();
            if(!ibdFrmDir.endsWith(CommonConstant.SEPARATOR)){
                ibdFrmDir+=CommonConstant.SEPARATOR;
            }
            ApiClient apiClient = K8sClientManager.get(prop.getK8sConfigId());
            CoreV1Api api = new CoreV1Api(apiClient);
            Exec exec = new Exec(apiClient);
            V1Pod v1Pod = api.readNamespacedPod(prop.getPodName(), prop.getNamespace(),null);
            boolean existsDir = K8sExecUtil.existsDir(exec,v1Pod,ibdFrmDir);
            if(!existsDir){
                return Result.error("ibd/frm："+ibdFrmDir+"目录不存在，请检查配置");
            }
            String storeDataDir = ibdFrmDir+mysqlUser.getIp()+"_"+ DateUtils.formatDateForYyyymmddhhmmss(new Date())
                    +CommonConstant.SEPARATOR;
            String dir = getDataDir(connection);
            if(!dir.endsWith(CommonConstant.SEPARATOR)){
                dir+=CommonConstant.SEPARATOR;
            }
            dir+=dbEscapeConfig.getDatabaseDir(clearDataConfig.getDbDatabase());
            log.info("database dir:{}",dir);
            String baseDir = dir;
            log.info("storeDataDir:{}",storeDataDir);
            try{
                int hour = LocalDateTime.now().getHour();
                if(checkTime&&hour>=6&&hour<=21){
                    String currentDate = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT);
                    return Result.error("java清理数据异常：<br/>清理不在允许的时间段内，请检查配置或线程池是否有残余任务，当前时间为："+currentDate);
                }
                StringBuilder cpCommand = new StringBuilder();
                cpCommand.append("mkdir -p ");
                cpCommand.append(storeDataDir);
                cpCommand.append(" && ");
                cpCommand.append("cp -rf ");
                for(String tableName:clearTableName){
                    String idbFilePath = baseDir+CommonConstant.SEPARATOR+tableName+CommonConstant.IBD_SUFFIX;
                    String frmfilePath = baseDir+CommonConstant.SEPARATOR+tableName+CommonConstant.FRM_SUFFIX;
                    cpCommand.append(idbFilePath).append(" ").append(frmfilePath).append(" ");
                }
                cpCommand.append(" ").append(storeDataDir);
                log.info("cpCommand:{}",cpCommand.toString());
                ExecResult execResult = K8sExecUtil.doExec(exec,v1Pod,new String[]{"sh","-c",cpCommand.toString()});
                if(execResult.getCode()==null||execResult.getCode()!=0){
                    return Result.error("ibd/frm：备份失败，详细日志："+execResult.getMessage());
                }
                List<String> dirList = K8sExecUtil.ls(exec,v1Pod,storeDataDir);
                long fileLength = dirList.stream().filter(fileName->fileName.contains(CommonConstant.IBD_SUFFIX)||
                        fileName.contains(CommonConstant.FRM_SUFFIX)).count();
                log.info("clearTableName.size：{}",clearTableName.size());
                log.info("ibd/frm length：{}",fileLength);
                if(clearTableName.size()*2!=fileLength){
                    return Result.error("ibd/frm：备份失败，备份文件个数与表个数不一致，备份文件个数："+ fileLength+"，表个数：{}"+clearTableName.size());
                }
            }catch (Exception e){
                log.error("backup file error",e);
                K8sExecUtil.rmDir(exec,v1Pod,storeDataDir);
                throw e;
            }
            return Result.OK(storeDataDir);
        }catch (Exception e){
            log.error("备份idb和frm失败",e);
            return Result.error("备份idb和frm失败："+e.toString());
        }
    }

    @Override
    public Page<Map<String, String>> getIbdFrm(Page<Map<String, String>> page, DatasourceProp prop, MysqlUser mysqlUser, String ibdfrmPath) {
        try{
            ApiClient apiClient = K8sClientManager.get(prop.getK8sConfigId());
            Exec exec = new Exec(apiClient);
            ExecResult execResult = K8sExecUtil.doExec(exec,prop.getNamespace(),prop.getPodName(),new String[]{"sh","-c","cd "+ibdfrmPath+" && ls -l"});
            String dirs = execResult.getMessage();
            if(!CommonConstant.EXEC_SUCCESS.equals(execResult.getCode())){
                throw new BusinessException("获取ibdfrm失败");
            }
            String[] fileStrs = dirs.split("\\n");
            List<Map<String,String>> targetList = Arrays.stream(fileStrs).skip((page.getCurrent()-1)*page.getSize()+1).limit(page.getSize())
                    .map(line->{
                        List<String> fileInfo = Arrays.asList(line.split(" ")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                        Map<String,String> map = new HashMap<>();
                        long size = Long.parseLong(fileInfo.get(4));
                        map.put("fileName",fileInfo.get(8));
                        map.put("fileSize", CommonUtils.getNetFileSizeDescription(size));
                        return map;
                    })
                    .collect(Collectors.toList());
            page.setRecords(targetList);
            page.setTotal(fileStrs.length-1);
            return page;
        }catch (Exception e){
            log.error("获取ibdfrm失败",e);
            throw new BusinessException("获取ibdfrm失败");
        }
    }

    @Override
    public Result checkSoftInstall(BackupConfig backupConfig,DatasourceProp prop) {
        List<Boolean> list = new ArrayList<>();
        ApiClient apiClient = K8sClientManager.get(prop.getK8sConfigId());
        CoreV1Api api = new CoreV1Api(apiClient);
        V1Pod v1Pod = null;
        try{
            v1Pod = api.readNamespacedPod(prop.getPodName(), prop.getNamespace(),null);
            list.add(true);
        }catch (Exception e){
            log.error("error",e);
            list.add(false);
        }
        Exec exec = new Exec(apiClient);
        try {
            //实际上是没有qpress -h这个命令的，有qpress -v这个命令，但是qpress的相关命令都会卡主，不知道为什么
            //这里采用qpress -h判断是否安装了qpress
            ExecResult execResult = K8sExecUtil.doExec(exec,v1Pod,new String[]{"sh","-c","qpress -h"});
            list.add(execResult.getCode()!=null&&execResult.getCode()==255);
        }catch (Exception e){
            log.error("error",e);
            list.add(false);
        }
        try {
            list.add(K8sExecUtil.existsDir(exec,v1Pod,backupConfig.getBackupPath()));
        }catch (Exception e){
            log.error("error",e);
            list.add(false);
        }
        String url = "jdbc:mysql://"+prop.getIp()+":"+prop.getPort();
        String password = PwdUtil.decryptPwd(prop.getPassword());
        try(Connection connection = JdbcUtil.getConnection(url,
                prop.getUser(),password)){
            if(CollectionUtils.isNotEmpty(backupConfig.getBackupDatabases())){
                String sql = "select schema_name from information_schema.SCHEMATA";
                List<String> allDatabases = JdbcUtil.queryByList(connection,sql,null,rs->rs.getString(1));
                boolean match = backupConfig.getBackupDatabases().stream().allMatch(allDatabases::contains);
                list.add(match);
            }else{
                list.add(true);
            }
        }catch(Exception e){
            list.add(false);
        }
        return Result.OK(list);
    }


    private String deleteOldAllTarFiles(Exec exec,V1Pod v1Pod,String backupPath,List<String> allTarFiles,Integer keepDays) throws Exception{
        StringBuilder sb = new StringBuilder();
        for(String fileName:allTarFiles){
            Date allFileTime = DateUtil.parse(fileName.substring(3,17), DatePattern.PURE_DATETIME_PATTERN);
            long diff = DateUtil.betweenDay(allFileTime , new Date(),true);
            if(diff>keepDays){
                boolean success = K8sExecUtil.rmDir(exec,v1Pod,backupPath+CommonConstant.SEPARATOR+fileName);
                if(success){
                    sb.append("删除备份文件：").append(backupPath+CommonConstant.SEPARATOR+fileName).append(" 成功<br/>");
                }else{
                    sb.append("删除备份文件：").append(backupPath+CommonConstant.SEPARATOR+fileName).append(" 失败,<br/>");
                }
            }
        }
        return sb.toString();
    }

    private String getDataDir(Connection connection){
        String sql = "show variables like 'datadir'";
        List<String> dir = JdbcUtil.queryByList(connection,sql,null,
                rs-> rs.getString(2));
        return dir.get(0);
    }

    private void tarOldFullFile(Exec exec,V1Pod v1Pod,String backupPath, String lastFullTimeStr, BackupResult backupResult) throws Exception {
        List<String> matchFiles = getLastBackupFullAndIncrFile(exec,v1Pod,backupPath,Long.parseLong(lastFullTimeStr));
        String allFileDir = "all"+lastFullTimeStr;
        String filesStr = matchFiles.stream().collect(Collectors.joining(" "));
        StringBuilder mvCommand = new StringBuilder("cd ");
        mvCommand.append(backupPath).append(" && mkdir -p "+backupPath+CommonConstant.SEPARATOR+allFileDir)
                .append(" && mv ").append(filesStr).append(" ")
                .append(allFileDir).append(CommonConstant.SEPARATOR).append(" && tar -zcvf ").
                append(allFileDir).append(".tar.gz ").append(allFileDir)
                .append(" && rm -rf "+allFileDir);
        ExecResult execResult = K8sExecUtil.doExec(exec,v1Pod,new String[]{"sh","-c",mvCommand.toString()});
        log.info("mvCommand:{}",mvCommand);
        if(execResult.getCode()==null||execResult.getCode()!=0){
            backupResult.setNewTarFile("打包压缩全备和增备文件失败，请进行人工排查，打包命令为："+mvCommand.toString());
        }else{
            backupResult.setNewTarFile("打包压缩全备和增备文件成功："+backupPath+"/"+allFileDir+".tar.gz ");
        }

    }

    /**
     * 获取上次全量备份和增量备份的全部文件
     */
    private List<String> getLastBackupFullAndIncrFile(Exec exec,V1Pod v1Pod, String backupPath, long lastFullTime) throws Exception{
        long endTime = Long.parseLong(DateUtils.formatDateForYyyymmddhhmmss(new Date()));
        List<String> dirs = K8sExecUtil.ls(exec,v1Pod,backupPath);
        List<String> matchFileName = dirs.stream().filter(dir->{
            if(dir.contains("incr")||dir.contains("full")){
                long fileTime = Long.parseLong(dir.substring(4));
                return lastFullTime<=fileTime&&fileTime<=endTime;
            }
            return false;
        }).collect(Collectors.toList());
        return matchFileName;
    }

    @Override
    public Result installSoftware(BackupConfig backupConfig, DatasourceProp prop, String type) {
        throw new BusinessException("k8s暂不支持此方法");
    }

    @Override
    public Result testSsh(BackupConfig backupConfig) {
        throw new BusinessException("k8s暂不支持此方法");
    }
    @Override
    public Result recoverPrepare(BackupConfig backupConfig,DatasourceProp prop) {
        throw new BusinessException("k8s暂不支持此方法");
    }
}
