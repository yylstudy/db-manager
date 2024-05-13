package com.linkcircle.dbmanager.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.boot.common.util.CommonUtils;
import com.linkcircle.boot.common.util.DateUtils;
import com.linkcircle.boot.thread.ThreadPoolFactory;
import com.linkcircle.dbmanager.common.BackupResult;
import com.linkcircle.dbmanager.common.BackupType;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.ExecResult;
import com.linkcircle.dbmanager.config.DbEscapeConfig;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.BackupConfigMapper;
import com.linkcircle.dbmanager.service.BackupClearDataService;
import com.linkcircle.dbmanager.service.ComputerRoomService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.dbmanager.service.MysqlUserService;
import com.linkcircle.dbmanager.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/24 14:35
 */
@Slf4j
@Service("sshBackupServiceImpl")
public class SshBackupClearDataServiceImpl implements BackupClearDataService {
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Autowired
    private DbEscapeConfig dbEscapeConfig;
    @Autowired
    private BackupConfigMapper backupConfigMapper;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private ComputerRoomService computerRoomService;
    @Value("${software.server.host}")
    private String softwareHost;
    @Value("${software.server.port}")
    private Integer softwarePort;
    @Value("${software.server.username}")
    private String softwareUsername;
    @Value("${software.server.password}")
    private String softwarePassword;
    @Override
    public void checkSoftInstallResult(BackupConfig backupConfig, DatasourceProp prop) {
        Result result = checkSoftInstall(backupConfig,prop);
        List<Boolean> checkResultList = (List<Boolean>)result.getResult();
        StringBuilder checkResult = new StringBuilder();
        if(!checkResultList.get(0)){
            checkResult.append("未安装xtrabackup、");
        }
        if(!checkResultList.get(1)){
            checkResult.append("未安装qpress、");
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
    public Result recoverPrepare(BackupConfig backupConfig,DatasourceProp prop) {
        String recoverTarFile = backupConfig.getRecoverTarFile();
        //本地文件，则查询数据库
        if(CommonConstant.LOCALHOST_FILE.equals(backupConfig.getIslocalhostFile())){
            backupConfig = backupConfigMapper.selectById(backupConfig.getId());
            setSsh(backupConfig);
            if(recoverTarFile.contains(backupConfig.getBackupPath())){
                throw new BusinessException("还原文件在备份目录下，请修改还原文件路径");
            }
            backupConfig.setRecoverTarFile(recoverTarFile);
        }
        if(!recoverTarFile.endsWith("tar.gz")){
            throw new BusinessException("还原文件不是tar.gz，请检查");
        }
        String mysqlCnf = backupConfig.getMysqlCnf();
        Session session = null;
        try{
            session = SshUtil.getSession(backupConfig.getMysqlSshHost(),backupConfig.getMysqlSshPort(),
                    backupConfig.getMysqlSshUser(),backupConfig.getMysqlSshPassword());
            Session finalSession = session;
            Result result = SshUtil.doExecute(session, channelSftp -> {
                try {
                    channelSftp.stat(recoverTarFile);
                } catch (Exception e) {
                    throw new BusinessException("还原文件："+recoverTarFile+"不存在，请检查文件");
                }
                int index = recoverTarFile.lastIndexOf(CommonConstant.SEPARATOR);
                String simpleFileName = recoverTarFile.substring(index+1);
                String dir = simpleFileName.replaceAll(".tar.gz","");
                String recoverTarDir = recoverTarFile.substring(0,index);
                String command = "cd "+recoverTarDir+" && rm -rf "+dir+" && mkdir "+dir+" && tar -xzvf "+recoverTarFile+" -C "+dir+" --strip-components 1";
                Integer xzvfCode = SshUtil.getExecStatus(finalSession,command);
                if(xzvfCode!=null&&xzvfCode==0){
                    String recoverDileDir = recoverTarDir+CommonConstant.SEPARATOR+dir+CommonConstant.SEPARATOR;
                    List<ChannelSftp.LsEntry> files = channelSftp.ls(recoverDileDir);
                    String fullFileName = "";
                    TreeMap<Long,String> treeMap = new TreeMap();
                    for(ChannelSftp.LsEntry entry:files){
                        String fileName = entry.getFilename();
                        if(fileName.contains(CommonConstant.FULLPREFIX)){
                            if(StringUtils.isNotBlank(fullFileName)){
                                throw new BusinessException("存在多个全量备份文件");
                            }
                            fullFileName = fileName;
                        }
                        if(fileName.contains(CommonConstant.FULLPREFIX)||fileName.contains(CommonConstant.INCRPREFIX)){
                            Long time = Long.parseLong(fileName.substring(4));
                            treeMap.put(time,fileName);
                        }
                    }
                    String name = treeMap.firstEntry().getValue();
                    if(!fullFileName.equals(name)){
                        throw new BusinessException("存在时间比全量文件还小的增量文件"+name);
                    }
                    String fullName = recoverDileDir+fullFileName;
                    String prepareLog = recoverDileDir+"prepare.log 2>&1";
                    String defaultFile = "--defaults-file="+mysqlCnf;
                    StringBuilder prepareCommand = new StringBuilder();
                    prepareCommand.append("xtrabackup ").append(defaultFile)
                            .append(" --decompress --remove-original --parallel=4 --target-dir=")
                            .append(fullName).append("  > ").append(prepareLog)
                            .append(" && xtrabackup ").append(defaultFile)
                            .append(" --prepare --apply-log-only --target-dir=")
                            .append(fullName).append(" >> ").append(prepareLog);
                    treeMap.values().stream().skip(1).forEach(fileName->{
                        prepareCommand.append(" && xtrabackup ").append(defaultFile)
                                .append(" --decompress --remove-original --parallel=4 --target-dir=")
                                .append(recoverDileDir).append(fileName).append("  >> ").append(prepareLog)
                                .append(" && xtrabackup ").append(defaultFile)
                                .append(" --prepare --apply-log-only --target-dir=").append(fullName)
                                .append(" --incremental-dir=").append(recoverDileDir+fileName).append("  >> ").append(prepareLog);
                    });
                    Integer prepareFullCode = SshUtil.getExecStatus(finalSession,prepareCommand.toString());
                    InputStream preparelogIs = channelSftp.get(recoverDileDir+"prepare.log");
                    String preparelog = IOUtils.toString(preparelogIs,"utf-8");
                    preparelog = preparelog.replace("\n","<br/>");
                    if(prepareFullCode!=null&&prepareFullCode==0){
                        int count = StringUtils.countMatches(preparelog, CommonConstant.SUCCESS_FLAG);
                        channelSftp.rm(recoverDileDir+"prepare.log");
                        if(StringUtils.isNotBlank(preparelog)&&count==treeMap.size()*2){
                            return Result.OK("<span style=\"color:green\">还原准备成功;</span><br/>请关闭mysql服务备份并清空mysql data目录，并执行复制语句"+
                                    "<br/> <span style=\"color:red\"> xtrabackup "+defaultFile+" --copy-back --target-dir="+fullName+"</span>"
                                    +"<br/>"+preparelog,null);
                        }else{
                            return Result.error("<span style=\"color:red\">还原准备失败;</span><br/>明细日志：<br/>"+preparelog);
                        }
                    }else{
                        return Result.error("<span style=\"color:red\">还原准备命令返回失败;</span><br/>明细日志：<br/>"+preparelog);
                    }
                }else{
                    throw new BusinessException("解压还原文件"+recoverTarFile+"失败");
                }
            });
            return result;
        }catch (Exception e){
            log.error("还原备份异常：",e);
            return Result.error("还原备份异常："+e.getMessage());
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }


    @Override
    public Result backupIbdAndFrm(ClearDataConfig clearDataConfig, DatasourceProp datasourceProp, Connection connection, List<String> clearTableName, boolean checkTime) {
        Session session = null;
        try{
            MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
            int port = datasourceProp.getSshPort();
            String sshUser = datasourceProp.getSshUser();
            String sshPassword = datasourceProp.getSshPassword();
            String ibdFrmDir = clearDataConfig.getIbdFrmDir();
            if(!ibdFrmDir.endsWith(CommonConstant.SEPARATOR)){
                ibdFrmDir+=CommonConstant.SEPARATOR;
            }
            session = SshUtil.getSession(mysqlUser.getIp(),port, sshUser,PwdUtil.decryptPwd(sshPassword));
            Integer execStatus = SshUtil.getExecStatus(session,"cd "+ibdFrmDir);
            if(execStatus==null||execStatus!=0){
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
                SshUtil.doExecute(session,channelSftp -> {
                    channelSftp.mkdir(storeDataDir);
                    return null;
                });
                int hour = LocalDateTime.now().getHour();
                if(checkTime&&hour>=6&&hour<=21){
                    String currentDate = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT);
                    return Result.error("java清理数据异常：<br/>清理不在允许的时间段内，请检查配置或线程池是否有残余任务，当前时间为："+currentDate);
                }
                StringBuilder cpCommand = new StringBuilder();
                if(datasourceProp.isSudoUser()){
                    cpCommand.append("sudo ");
                }
                cpCommand.append("cp -rf ");
                for(String tableName:clearTableName){
                    String idbFilePath = baseDir+CommonConstant.SEPARATOR+tableName+CommonConstant.IBD_SUFFIX;
                    String frmfilePath = baseDir+CommonConstant.SEPARATOR+tableName+CommonConstant.FRM_SUFFIX;
                    cpCommand.append(idbFilePath).append(" ").append(frmfilePath).append(" ");
                }
                cpCommand.append(" ").append(storeDataDir);
                log.info("cpCommand:{}",cpCommand.toString());
                ExecResult execResult = SshUtil.getExecResult(session,cpCommand.toString());
                if(execResult.getCode()==null||execResult.getCode()!=0){
                    return Result.error("ibd/frm：备份失败，详细日志："+execResult.getErrorMessage());
                }
                long fileLength = SshUtil.doExecute(session,channelSftp -> {
                    List<ChannelSftp.LsEntry> list = channelSftp.ls(storeDataDir);
                    return list.stream().filter(entry->{
                        String fileName = entry.getFilename();
                        return fileName.contains(CommonConstant.IBD_SUFFIX)||
                                fileName.contains(CommonConstant.FRM_SUFFIX);
                    }).count();
                });
                log.info("clearTableName.size：{}",clearTableName.size());
                log.info("ibd/frm length：{}",fileLength);
                if(clearTableName.size()*2!=fileLength){
                    return Result.error("ibd/frm：备份失败，备份文件个数与表个数不一致，备份文件个数："+ fileLength+"，表个数：{}"+clearTableName.size());
                }
            }catch (Exception e){
                log.error("backup file error",e);
                SshUtil.doExecute(session,channelSftp -> {
                    channelSftp.rm(storeDataDir);
                    return null;
                });
                throw e;
            }
            return Result.OK(storeDataDir);
        }catch (Exception e){
            log.error("备份idb和frm失败",e);
            if(session!=null){
                session.disconnect();
            }
            return Result.error("备份idb和frm失败："+e.toString());
        }
    }

    @Override
    public BackupResult doBackup(BackupConfig backupConfig, DatasourceProp prop, boolean checkTime) {
        Session session = null;
        BackupResult backupResult = new BackupResult();
        try{
            checkSoftInstallResult(backupConfig,prop);
            session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),
                    prop.getSshUser(),PwdUtil.decryptPwd(prop.getSshPassword()));
            Session finalSession = session;
            String basePath = backupConfig.getBackupPath();
            //备份路径以/结尾，则去除/
            if(backupConfig.getBackupPath().endsWith(CommonConstant.SEPARATOR)){
                basePath = basePath.substring(0,basePath.length()-1);
            }
            String backupPath = basePath;
            String fullPrefix = CommonConstant.FULLPREFIX;
            String incrPrefix = CommonConstant.INCRPREFIX;
            String allPrefix = CommonConstant.ALLRPREFIX;
            String successFlag = CommonConstant.SUCCESS_FLAG;
            SshUtil.doExecute(session, channelSftp -> {
                String backlogPath = backupPath+ CommonConstant.SEPARATOR +"backupLog";
                List<ChannelSftp.LsEntry> list = channelSftp.ls(backupPath);
                String maxTimeFile = "";
                String maxTimeFullFile = "";
                List<String> allTarFiles = new ArrayList<>();
                //增量和全量目录命名规则为 fullyyyyMMddHHmmss和incryyyyMMddHHmmss
                //找出最近的全量文件
                //找出最近的备份文件
                for(ChannelSftp.LsEntry entry:list){
                    String fileName = entry.getFilename();
                    if(fileName.contains(fullPrefix)){
                        maxTimeFullFile = FileUtil.getMaxTimeFile(maxTimeFullFile,fileName);
                    }
                    if(fileName.contains(incrPrefix)||fileName.contains(fullPrefix)){
                        maxTimeFile = FileUtil.getMaxTimeFile(maxTimeFile,fileName);
                    }
                    if(fileName.contains(allPrefix)){
                        allTarFiles.add(fileName);
                    }
                }
                SshUtil.deleteFile(channelSftp,backlogPath);
                StringBuilder sb = new StringBuilder();
                if(prop.isSudoUser()){
                    sb.append("sudo ");
                }
                sb.append("xtrabackup");
                Date date = new Date();
                String time = DateUtils.formatDateForYyyymmddhhmmss(new Date());
                String pwd = PwdUtil.decryptPwd(prop.getPassword());
                sb.append(" --backup --compress --compress-threads=4 ")
                        .append(" --host=").append(prop.getIp())
                        .append(" --port=").append(prop.getPort())
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
                            .append(" > ").append(backlogPath).append(" 2>&1");
                    backupResult.setBackupType(BackupType.FULL);
                    maxTimeFullFile = fullPrefix+time;
                }else{
                    //增量备份
                    backupResult.setBackupType(BackupType.INCR);
                    sb.append(" --target-dir=").append(backupPath+CommonConstant.SEPARATOR+incrPrefix+time)
                            .append(" --incremental-basedir=").append(backupPath+CommonConstant.SEPARATOR+maxTimeFile)
                            .append(" > ").append(backlogPath).append(" 2>&1");

                }
                String newBackFileName = backupPath+CommonConstant.SEPARATOR+(backupResult.getBackupType().getCode())+time;
                String backupCommand = sb.toString();
                log.info("backupCommand:{}",backupCommand);
                int hour = LocalDateTime.now().getHour();
                if(checkTime&&hour>=6&&hour<=21){
                    String currentDate = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT);
                    backupResult.setMsg("java执行备份异常：<br/>备份命令不在允许的时间段内，请检查配置或线程池是否有残余任务，当前时间为："+currentDate);
                    return backupResult;
                }
                ExecResult execResult = null;
                try{
                    execResult = SshUtil.getExecResult(finalSession,backupCommand);
                }catch (Exception e){
                    log.error("java执行备份命令异常",e);
                    SshUtil.deleteFile(channelSftp,newBackFileName);
                    backupResult.setMsg("java执行备份命令异常：<br/>"+ ExceptionUtil.getMessage(e));
                    return backupResult;
                }
                if(execResult.getCode()!=null&&execResult.getCode()==0){
                    String backlog = SshUtil.getFileStr(channelSftp,backlogPath);
                    SshUtil.deleteFile(channelSftp,backlogPath);
                    int count = StringUtils.countMatches(backlog,successFlag);
                    if(StringUtils.isNotBlank(backlog)&&count==1){
                        backupResult.setSuccess(true);
                        backupResult.setBackupDir(newBackFileName);
                        backupResult.setMsg(backlog);
                        String lastFullTimeStr = maxTimeFullFile.substring(4);
                        Date lastFullTime = DateUtil.parse(lastFullTimeStr, DatePattern.PURE_DATETIME_PATTERN);
                        long differDay = DateUtil.betweenDay(lastFullTime , date,true);
                        if("1".equals(prop.getEnableRemoteStore())){
                            ComputerRoom computerRoom = computerRoomService.getById(prop.getComputerRoomId());
                            String scpTarget = computerRoom.getBasePath();
                            if(!scpTarget.endsWith(CommonConstant.SEPARATOR)){
                                scpTarget+=CommonConstant.SEPARATOR;
                            }
                            scpTarget+=prop.getIp()+CommonConstant.SEPARATOR+CommonConstant.DEFAULT_BACKUP_DIR;
                            //scp到机房配置的ssh
                            scpComputerRoomSsh(computerRoom,backupPath,finalSession,prop,newBackFileName,scpTarget,backupResult);
                            //删除旧文件
                            deleteComputerRoomSshOldFile(computerRoom,scpTarget,backupConfig.getKeepDays(),backupResult);
                        }
                        //超过全量备份周期
                        if((differDay>=backupConfig.getDayBeforeFull()-1)){
                            tarOldFullFile(channelSftp,backupPath,lastFullTimeStr,finalSession,backupResult);
                        }
                        String deleteFiles = deleteOldAllTarFiles(backupPath,allTarFiles,backupConfig.getKeepDays(),channelSftp);
                        backupResult.setDeleteTarFile(deleteFiles);
                    }else{
                        SshUtil.getExecResult(finalSession,"rm -rf "+newBackFileName);
//                        SshUtil.deleteFile(channelSftp,newBackFileName);
                        backupResult.setMsg("备份日志未返回成功，详细日志为：<br/>"+backlog);
                    }
                }else{
                    String backlog = SshUtil.getFileStr(channelSftp,backlogPath);
                    SshUtil.getExecResult(finalSession,"rm -rf "+newBackFileName);
//                    SshUtil.deleteFile(channelSftp,newBackFileName);
                    String errorMsg = "执行备份命令未返回成功，执行命令日志返回为："+execResult.getErrorMessage()+"<br/>";
                    if(StringUtils.isNotBlank(backlog)){
                        errorMsg+="备份日志返回为："+backlog+"<br/>";
                    }
                    backupResult.setMsg(errorMsg);
                }
                return null;
            });
        }catch (Exception e){
            log.error("备份失败",e);
            backupResult.setMsg("备份过程中出现异常："+ ExceptionUtil.getMessage(e));
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
        return backupResult;
    }


    @Override
    public Page<Map<String, String>>  getIbdFrm(Page<Map<String, String>> page, DatasourceProp prop, MysqlUser mysqlUser, String ibdfrmPath) {
        Session session = null;
        try{
            int port = prop.getSshPort();
            String sshUser = prop.getSshUser();
            String sshPassword = prop.getSshPassword();
            session = SshUtil.getSession(mysqlUser.getIp(),port, sshUser, PwdUtil.decryptPwd(sshPassword));
            return SshUtil.doExecute(session,channelSftp -> {
                List<ChannelSftp.LsEntry> list = channelSftp.ls(ibdfrmPath);
                List<ChannelSftp.LsEntry> fileList = list.stream().filter(entry->{
                    String fileName = entry.getFilename();
                    return fileName.contains(CommonConstant.IBD_SUFFIX)||
                            fileName.contains(CommonConstant.FRM_SUFFIX);
                }).collect(Collectors.toList());
                List<Map<String,String>> targetList = fileList.stream().skip((page.getCurrent()-1)*page.getSize()).limit(page.getSize())
                        .map(entry->{
                            Map<String,String> map = new HashMap<>();
                            long size = entry.getAttrs().getSize();
                            map.put("fileName",entry.getFilename());
                            map.put("fileSize", CommonUtils.getNetFileSizeDescription(size));
                            return map;
                        })
                        .collect(Collectors.toList());
                page.setRecords(targetList);
                page.setTotal(fileList.size());
                return page;
            });
        }catch (Exception e){
            log.error("获取ibd/frm失败",e);
            throw new BusinessException("获取ibd/frm失败");
        }finally{
            if(session!=null){
                session.disconnect();
            }
        }

    }

    private String getDataDir(Connection connection){
        String sql = "show variables like 'datadir'";
        List<String> dir = JdbcUtil.queryByList(connection,sql,null,
                rs-> rs.getString(2));
        return dir.get(0);
    }

    @Override
    public Result installSoftware(BackupConfig backupConfig, DatasourceProp prop, String type) {
        Session session = null;
        try{
            InputStream softIs = getSoftwareIs(type);
            session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),
                    prop.getSshUser(),PwdUtil.decryptPwd(prop.getSshPassword()));
            StringBuilder basePath = new StringBuilder("/");
            SshUtil.doExecute(session, channelSftp -> {
                if(!CommonConstant.ROOT_USER.equals(prop.getSshUser())){
                    basePath.append("home");
                }
                basePath.append("/"+prop.getSshUser());
                String filename;
                if(CommonConstant.INSTALL_XTRABACKUP.equals(type)){
                    filename = basePath+"/percona-xtrabackup-24-2.4.9-1.el7.x86_64.rpm";
                }else{
                    filename = basePath+"/qpress-11-linux-x64.tar";
                }
                channelSftp.put(softIs,filename);
                return null;
            });
            String commandStr = "";
            String suCommand = " echo \""+PwdUtil.decryptPwd(prop.getSshPassword())+"\" |  sudo -S ";
            if(CommonConstant.INSTALL_XTRABACKUP.equals(type)){
                commandStr = "cd "+basePath+" && "+suCommand+" yum install -y percona-xtrabackup-24-2.4.9-1.el7.x86_64.rpm  ";
            }else if(CommonConstant.INSTALL_QPRESS.equals(type)){
                commandStr = "cd "+basePath+"  &&  rm -rf qpress && "+suCommand+" rm -rf /usr/bin/qpress &&  tar xvf qpress-11-linux-x64.tar " +
                        " && "+suCommand+" ln -s "+basePath+"/qpress /usr/bin";
            }else {
                return Result.error("无效的软件安装类型");
            }
            log.info("install software:{}",commandStr);
            ExecResult execResult = SshUtil.getExecResult(session,commandStr);
            if(execResult.getCode()==null||execResult.getCode()!=0){
                log.error(type+"安装失败，"+execResult.getErrorMessage());
                return Result.error(101,type+"安装失败，"+execResult.getErrorMessage());
            }
            try{
                checkSoftInstall(backupConfig,prop);
            }catch (Exception e){
                log.error("active error");
            }
            log.info("install "+type+" success");
            return Result.OK(type+"安装成功",null);
        }catch (Exception e){
            log.error(type+"安装失败，请联系管理员",e);
            if(e instanceof BusinessException){
                throw (BusinessException)e;
            }
            return Result.error(type+"安装失败，请联系管理员");
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

    /**
     * scp到机房的ssh存储上
     * @param backupPath
     * @param session
     * @param prop
     * @param srcFile
     * @return
     * @throws Exception
     */
    private ExecResult scpComputerRoomSsh(ComputerRoom computerRoom,String backupPath,Session session,
                                          DatasourceProp prop,String srcFile,String scpTarget,BackupResult backupResult) throws Exception{
        mkdirRemoteDir(computerRoom,scpTarget);
        String shellName = backupPath+CommonConstant.SEPARATOR+CommonConstant.DEFAULT_SHELL_NAME;
        long start = System.currentTimeMillis();
        String commandStr = "cat > "+shellName+" <<\\EOF \n" +
                "#!/usr/bin/expect\n" +
                "set timeout "+CommonConstant.EXPECT_DEFAULT_TIMEOUT+" \n" +
                "spawn scp -P "+computerRoom.getSshPort()+" -l 600000 -r "+srcFile+" "
                +computerRoom.getSshUsername()+"@"+computerRoom.getSshIp()+":"+scpTarget+" \n" +
                "expect {\n" +
                " \"yes/no\" {\n" +
                "    send \"yes\\r\"\n" +
                "    expect \"password:\"\n" +
                "    send \""+PwdUtil.decryptPwd(computerRoom.getSshPassword())+"\\r\"\n" +
                "  }\n" +
                " \"password\" {send \""+PwdUtil.decryptPwd(computerRoom.getSshPassword())+"\\r\"}\n" +
                "}\n" +
                "\n" +
                "expect eof\n"+
                "EOF";
        CompletableFuture<ExecResult> scpFuture = CompletableFuture.supplyAsync(()->{
            log.info("create shell command:{}",commandStr);
            ExecResult execResult = SshUtil.getExecResult(session,commandStr);
            log.info("create shell result :{}",execResult);
            if(execResult.getCode()==null||execResult.getCode()!=0){
                return execResult;
            }
            String scpCommand = "expect "+shellName;
            log.info("scpCommand :{}",scpCommand);
            execResult = SshUtil.getExecResult(session,scpCommand);
            log.info("scpCommand result code:{}",execResult.getCode());
            return execResult;
        },ThreadPoolFactory.getScpThreadPool());
        deleteScpShellFile(prop,backupPath);
        ExecResult execResult = scpFuture.get();
        backupResult.setRemoteStoreMsg(execResult.getMessage());
        backupResult.setRemoteStoreTimeCost(System.currentTimeMillis()-start);
        backupResult.setEnableRemoteStore(true);
        backupResult.setRemoteStoreSuccess(execResult.getCode()!=null&&execResult.getCode()==0);
        return execResult;
    }

    /**
     * 删除机房远程存储旧的备份文件
     * @param computerRoom
     * @param scpTarget
     * @param keepDays
     * @return
     * @throws Exception
     */
    private void deleteComputerRoomSshOldFile(ComputerRoom computerRoom,String scpTarget,int keepDays,
                                                BackupResult backupResult) throws Exception{
        Session session = null;
        try{
            session = SshUtil.getSession(computerRoom.getSshIp(),computerRoom.getSshPort(),
                    computerRoom.getSshUsername(),PwdUtil.decryptPwd(computerRoom.getSshPassword()));
            String fullPrefix = CommonConstant.FULLPREFIX;
            String incrPrefix = CommonConstant.INCRPREFIX;
            Session finalSession = session;
            String res = SshUtil.doExecute(session,channelSftp -> {
                log.info("scpTarget:{}",scpTarget);
                List<ChannelSftp.LsEntry> list = channelSftp.ls(scpTarget);
                List<String> allFullIncrFiles = list.stream().filter(entry->entry.getFilename()
                        .contains(fullPrefix)||entry.getFilename().contains(incrPrefix))
                        .map(ChannelSftp.LsEntry::getFilename).collect(Collectors.toList());
                String deleteOldFullIncrFiles = deleteOldFullIncrFiles(scpTarget,allFullIncrFiles,keepDays,finalSession);
                backupResult.setDeleteRemoteFile(deleteOldFullIncrFiles);
                return null;
            });
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }


    private void deleteScpShellFile(DatasourceProp prop,String backupPath){
        CompletableFuture.supplyAsync(()->{
            Session session = null;
            try{
                session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),prop.getSshUser(),PwdUtil.decryptPwd(prop.getSshPassword()));
                //睡眠30s等待scpComputerRoomSsh开始执行
                TimeUnit.SECONDS.sleep(30);
                String deleteScpShellFileCommand = "cd "+backupPath+" && rm -rf "+CommonConstant.DEFAULT_SHELL_NAME;
                log.info("deleteScpShellFileCommand:{}",deleteScpShellFileCommand);
                ExecResult execResult = SshUtil.getExecResult(session,deleteScpShellFileCommand);
                log.info("deleteScpShellFileCommand result :{}",execResult);
                return execResult;
            }catch (Exception e){
                log.error("delete scp shell error",e);
            }finally {
                if(session!=null){
                    session.disconnect();
                }
            }
            return null;

        },ThreadPoolFactory.getScpThreadPool());
    }

    private void mkdirRemoteDir(ComputerRoom computerRoom,String scpTarget){
        Session session = null;
        try{
            session = SshUtil.getSession(computerRoom.getSshIp(),computerRoom.getSshPort(),computerRoom.getSshUsername()
                    ,PwdUtil.decryptPwd(computerRoom.getSshPassword()));
            String mkdirCommand = "mkdir -p "+scpTarget;
            log.info("mkdirCommand:{}",mkdirCommand);
            ExecResult execResult = SshUtil.getExecResult(session,mkdirCommand);
            log.info("mkdirCommand result :{}",execResult);
        }catch (Exception e){
            log.error("delete scp shell error",e);
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }


    @Override
    public Result testSsh(BackupConfig backupConfig) {
        String host;
        if(backupConfig.getPropId()!=null){
            DatasourceProp prop = datasourcePropService.getById(backupConfig.getPropId());
            host = prop.getIp();
            setSsh(backupConfig);
        }else{
            host = backupConfig.getMysqlSshHost();
        }
        Session session = null;
        try{
            session = SshUtil.getSession(host,backupConfig.getMysqlSshPort(),
                    backupConfig.getMysqlSshUser(),backupConfig.getMysqlSshPassword());
            return Result.OK("ssh连接成功",null);
        }catch (Exception e){
            return Result.error("ssh连接失败");
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

    @Override
    public Result checkSoftInstall(BackupConfig backupConfig,DatasourceProp prop) {
        Session session = null;
        try{
            List<Boolean> list = new ArrayList<>();
            session = SshUtil.getSession(prop.getIp(),prop.getSshPort(),
                    prop.getSshUser(), PwdUtil.decryptPwd(prop.getSshPassword()));
            Integer execStatus = SshUtil.getExecStatus(session,"xtrabackup -v");
            list.add(execStatus!=null&&execStatus==0);
            execStatus = SshUtil.getExecStatus(session,"qpress");
            list.add(execStatus!=null&&execStatus==255);
            execStatus = SshUtil.getExecStatus(session,"cd "+backupConfig.getBackupPath());
            list.add(execStatus!=null&&execStatus==0);
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
            //开启远程存储，校验是否安装expect
            if("1".equals(prop.getEnableRemoteStore())){
                execStatus = SshUtil.getExecStatus(session,"expect -v");
                list.add(execStatus!=null&&execStatus==0);
            }
            return Result.OK(list);
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }

    }

    private InputStream getSoftwareIs(String type){
        Session session = null;
        ByteArrayOutputStream os =  new ByteArrayOutputStream();
        try{
            session = SshUtil.getSession(softwareHost,softwarePort,
                    softwareUsername,softwarePassword);
            SshUtil.doExecute(session, channelSftp -> {
                if(CommonConstant.INSTALL_XTRABACKUP.equals(type)){
                    channelSftp.get("/home/percona-xtrabackup-24-2.4.9-1.el7.x86_64.rpm",os);
                }else if(CommonConstant.INSTALL_QPRESS.equals(type)){
                    channelSftp.get("/home/qpress-11-linux-x64.tar",os);
                }else{
                    throw new BusinessException("无效的软件安装类型");
                }
                return null;
            });
            ByteArrayInputStream swapStream = new ByteArrayInputStream(os.toByteArray());
            return swapStream;
        }catch (Exception e){
            if(e instanceof BusinessException){
                throw (BusinessException)e;
            }
            throw new BusinessException("获取"+type+"软件安装包失败");
        } finally {
            if(session!=null){
                session.disconnect();
            }
        }

    }

    private void setSsh(BackupConfig backupConfig){
        DatasourceProp datasourceProp = datasourcePropService.getById(backupConfig.getPropId());
        backupConfig.setMysqlSshHost(datasourceProp.getIp());
        backupConfig.setMysqlSshUser(datasourceProp.getSshUser());
        backupConfig.setMysqlSshPort(datasourceProp.getSshPort());
        backupConfig.setMysqlSshPassword(PwdUtil.decryptPwd(datasourceProp.getSshPassword()));
    }
    private void tarOldFullFile(ChannelSftp channelSftp, String backupPath, String lastFullTimeStr, Session session, BackupResult backupResult) throws Exception{
        List<String> matchFiles = getLastBackupFullAndIncrFile(channelSftp,backupPath,Long.parseLong(lastFullTimeStr));
        String allFileDir = "all"+lastFullTimeStr;
        channelSftp.mkdir(backupPath+CommonConstant.SEPARATOR+allFileDir);
        String filesStr = matchFiles.stream().collect(Collectors.joining(" "));
        StringBuilder mvCommand = new StringBuilder("cd ");
        mvCommand.append(backupPath).append(" && mv ").append(filesStr).append(" ")
                .append(allFileDir).append(CommonConstant.SEPARATOR).append(" && tar -zcvf ").
                append(allFileDir).append(".tar.gz ").append(allFileDir)
                .append(" && rm -rf "+allFileDir);
        Integer mvCode = SshUtil.getExecStatus(session,mvCommand.toString());
        log.info("mvCommand:{}",mvCommand);
        if(mvCode==null||mvCode!=0){
            backupResult.setNewTarFile("打包压缩全备和增备文件失败，请进行人工排查，打包命令为："+mvCommand.toString());
        }else{
            backupResult.setNewTarFile("打包压缩全备和增备文件成功："+backupPath+"/"+allFileDir+".tar.gz ");
        }
    }

    private String deleteOldAllTarFiles(String backupPath,List<String> allTarFiles,Integer keepDays,ChannelSftp channelSftp){
        StringBuilder sb = new StringBuilder();
        for(String fileName:allTarFiles){
            Date allFileTime = DateUtil.parse(fileName.substring(3,17), DatePattern.PURE_DATETIME_PATTERN);
            long diff = DateUtil.betweenDay(allFileTime , new Date(),true);
            if(diff>keepDays){
                try {
                    channelSftp.rm(backupPath+CommonConstant.SEPARATOR+fileName);
                    sb.append("删除备份文件：").append(fileName).append(" 成功<br/>");
                }catch (Exception e){
                    log.error("删除备份文件："+fileName+" 失败",e);
                    sb.append("删除备份文件：").append(fileName).append(" 失败<br/>");
                }
            }
        }
        return sb.toString();
    }

    private String deleteOldFullIncrFiles(String backupPath,List<String> allFullIncrFiles,Integer keepDays,Session session){
        StringBuilder sb = new StringBuilder();
        for(String fileName:allFullIncrFiles){
            Date allFileTime = DateUtil.parse(fileName.substring(4,17), DatePattern.PURE_DATETIME_PATTERN);
            long diff = DateUtil.betweenDay(allFileTime , new Date(),true);
            String fullDeleteFilePath = backupPath+CommonConstant.SEPARATOR+fileName;
            if(diff>keepDays){
                try {
                    String deleteFileCommand = "cd "+backupPath+" && rm -rf "+fileName;
                    log.info("删除机房备份文件命令：{}",deleteFileCommand);
                    ExecResult execResult = SshUtil.getExecResult(session,deleteFileCommand);
                    if(execResult.getCode()!=null&&execResult.getCode()==0){
                        sb.append("删除机房备份文件：").append(fullDeleteFilePath).append(" 成功<br/>");
                    }else{
                        sb.append("删除机房备份文件：").append(fullDeleteFilePath).append(" 失败<br/>");
                        sb.append("失败原因："+execResult.getMessage()+"<br/>");
                    }

                }catch (Exception e){
                    log.error("删除机房备份文件："+backupPath+" 失败",e);
                    sb.append("删除机房备份文件：").append(backupPath).append(" 失败<br/>");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取上次全量备份和增量备份的全部文件
     * @param channelSftp
     */
    private List<String> getLastBackupFullAndIncrFile(ChannelSftp channelSftp,String backupPath,long lastFullTime){
        try{
            long endTime = Long.parseLong(DateUtils.formatDateForYyyymmddhhmmss(new Date()));
            List<ChannelSftp.LsEntry> list = channelSftp.ls(backupPath);
            List<String> matchFileName = list.stream().filter(entry->{
                String fileName = entry.getFilename();
                if(fileName.contains("incr")||fileName.contains("full")){
                    long fileTime = Long.parseLong(fileName.substring(4));
                    return lastFullTime<=fileTime&&fileTime<=endTime;
                }
                return false;
            }).map(ChannelSftp.LsEntry::getFilename).collect(Collectors.toList());
            return matchFileName;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
