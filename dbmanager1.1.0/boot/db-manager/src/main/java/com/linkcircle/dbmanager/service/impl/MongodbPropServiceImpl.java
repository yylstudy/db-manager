package com.linkcircle.dbmanager.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.ExecResult;
import com.linkcircle.dbmanager.common.MongoBackupResult;
import com.linkcircle.dbmanager.entity.MongodbProp;
import com.linkcircle.dbmanager.mapper.MongodbPropMapper;
import com.linkcircle.dbmanager.service.MongodbPropService;
import com.linkcircle.dbmanager.util.ExceptionUtil;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
@Slf4j
public class MongodbPropServiceImpl extends ServiceImpl<MongodbPropMapper, MongodbProp> implements MongodbPropService {
    @Override
    public IPage<MongodbProp> queryPage(Page<MongodbProp> page, MongodbProp mongodbProp) {
        return this.baseMapper.queryPage(page, mongodbProp);
    }

    @Override
    public MongoBackupResult backup(MongodbProp mongodbProp) {
        Session session = null;
        MongoBackupResult mongoBackupResult = new MongoBackupResult();
        try{
            String datetime = DateUtil.format(new Date(),DatePattern.PURE_DATETIME_PATTERN);
            String fullBackDir = mongodbProp.getBackupPath()+CommonConstant.SEPARATOR+datetime;
            String backlogPath = fullBackDir+ CommonConstant.SEPARATOR+"backupLog";
            String backCommand = "mkdir -p "+fullBackDir+" && mongodump -h "+mongodbProp.getIp()+":"+mongodbProp.getPort()
                    +" -o "+fullBackDir+" > "+backlogPath+"  2>&1";
            session = SshUtil.getSession(mongodbProp.getIp(),mongodbProp.getSshPort(),mongodbProp.getSshUser(), PwdUtil.decryptPwd(mongodbProp.getSshPassword()));
            Session finalSession = session;
            ExecResult execResult = SshUtil.getExecResult(session,backCommand);
            SshUtil.doExecute(session, channelSftp -> {
                String backlog = SshUtil.getFileStr(channelSftp,backlogPath);
                if(execResult.getCode()!=null&&execResult.getCode()==0){
                    SshUtil.deleteFile(channelSftp,backlogPath);
                    mongoBackupResult.setBackupDir(fullBackDir);
                    mongoBackupResult.setMsg(backlog);
                    mongoBackupResult.setSuccess(true);
                }else{
                    SshUtil.getExecResult(finalSession,"rm -rf "+fullBackDir);
                    String errorMsg = "执行备份命令未返回成功，执行命令日志返回为："+execResult.getErrorMessage()+"<br/>";
                    if(StringUtils.isNotBlank(backlog)){
                        errorMsg+="备份日志返回为："+backlog+"<br/>";
                    }
                    mongoBackupResult.setMsg(errorMsg);
                }
                return null;
            });

        }catch (Exception e){
            log.error("mongodb备份失败",e);
            mongoBackupResult.setMsg("mongodb备份过程中出现异常："+ ExceptionUtil.getMessage(e));
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
        return mongoBackupResult;
    }
}
