package com.linkcircle.dbmanager.thread;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.boot.service.CommonService;
import com.linkcircle.boot.thread.ThreadPoolFactory;
import com.linkcircle.dbmanager.common.*;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.BusinessMapper;
import com.linkcircle.dbmanager.mapper.DatasourcePropMapper;
import com.linkcircle.dbmanager.mapper.JobLogMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.ExceptionUtil;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.mapper.SysUserMapper;
import com.linkcircle.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * job trigger thread pool helper
 *
 * @author xuxueli 2018-07-03 21:08:07
 */
@Service
@Slf4j
public class JobTriggerService {
    @Autowired
    private BackupConfigService backupConfigService;
    @Autowired
    private JobLogMapper jobLogMapper;
    @Autowired
    private BusinessMapper businessMapper;
    @Autowired
    private DatasourcePropMapper datasourcePropMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private MongodbPropService mongodbPropService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private JobInfoService jobInfoService;
    @Autowired
    private ISysUserService sysUserService;

    /**
     * add trigger
     */
    public void addTrigger(JobInfo jobInfo,boolean checkTime) {
        ComputerRoom computerRoom = jobInfoService.getComputerRoom(jobInfo.getId());
        ThreadPoolFactory.getFastTriggerPool(computerRoom.getId()).execute(()->{
            JobLog jobLog = new JobLog();
            jobLog.setCreateTime(new Date());
            jobLog.setHandleCode(HandleEnum.UNDO.getCode());
            jobLog.setJobId(jobInfo.getId());
            jobLog.setStartDate(new Date());
            jobLogMapper.insert(jobLog);
            StringBuilder sb = new StringBuilder();
            try{
                String taskType = jobInfo.getTaskType();
                TaskTypeEnum taskTypeEnum = TaskTypeEnum.getTaskTypeEnum(taskType);
                Long propId;
                StringBuilder handleMsgBuilder = new StringBuilder();
                StringBuilder taskLog = new StringBuilder();
                Date start = new Date();
                String smsMsg;
                String jobName;
                String detailLog;
                boolean executeResult;
                if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
                    BackupConfig backupConfig = backupConfigService.getById(jobInfo.getConfigId());
                    handleMsgBuilder.append("备份名称："+backupConfig.getName()+"<br/>");
                    taskLog.append("mysql备份任务执行开始："+DateUtil.format(start, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    BackupResult result = backupConfigService.doBackup(backupConfig,checkTime);
                    Date end = new Date();
                    taskLog.append("mysql备份任务执行结束："+DateUtil.format(end, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    long ms = DateUtil.betweenMs(start,end);
                    taskLog.append("执行耗时："+ms+"ms<br/>");
                    taskLog.append("执行结果："+(result.isSuccess()?"<span style=\"color:green\">成功</span>":"<span style=\"color:red\">失败</span>")+"<br/>");
                    taskLog.append("备份类型："+result.getBackupType().getDesc()+"<br/>");
                    executeResult = result.isSuccess();
                    if(result.isSuccess()){
                        taskLog.append("生成备份文件路径："+result.getBackupDir()+"<br/>");
                        if(StringUtils.isNotBlank(result.getNewTarFile())){
                            taskLog.append(result.getNewTarFile()+"<br/>");
                        }
                        if(StringUtils.isNotBlank(result.getDeleteTarFile())){
                            taskLog.append(result.getDeleteTarFile());
                        }
                    }
                    jobLog.setHandleCode(result.isSuccess()?HandleEnum.SUCCESS.getCode():HandleEnum.FAIL.getCode());
                    propId = backupConfig.getPropId();
                    jobName = "备份任务："+backupConfig.getName();
                    smsMsg = "备份任务："+backupConfig.getName()+"执行完毕，执行结果："+(result.isSuccess()?"成功":"失败");
                    detailLog = result.getMsg();
                    detailLog = detailLog.replaceAll("\n","<br/>");
                }else if(taskTypeEnum==TaskTypeEnum.CLEAR_DATA){
                    ClearDataConfig clearDataConfig = clearDataConfigService.getById(jobInfo.getConfigId());
                    handleMsgBuilder.append("备份名称："+clearDataConfig.getName()+"<br/>");
                    taskLog.append("清理数据任务执行开始："+DateUtil.format(start, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    Result result = clearDataConfigService.clearData(clearDataConfig,checkTime);
                    Date end = new Date();
                    taskLog.append("清理数据任务执行结束："+DateUtil.format(end, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    long ms = DateUtil.betweenMs(start,end);
                    taskLog.append("执行耗时："+ms+"ms<br/>");
                    executeResult = result.isSuccess();
                    if(result.isSuccess()){
                        if(result.getResult()!=null){
                            jobLog.setIbdfrmPath(result.getResult().toString());
                            taskLog.append("ibd、frm存储路径："+result.getResult()+"<br/>");
                        }
                    }
                    taskLog.append("执行结果："+(result.isSuccess()?"<span style=\"color:green\">成功</span>":"<span style=\"color:red\">失败</span>")+"<br/>");
                    MysqlUser mysqlUser = mysqlUserService.getById(clearDataConfig.getMysqlUserId());
                    propId = mysqlUser.getPropId();
                    jobLog.setHandleCode(result.isSuccess()?HandleEnum.SUCCESS.getCode():HandleEnum.FAIL.getCode());
                    jobName = "清理任务："+clearDataConfig.getName();
                    smsMsg = "清理任务："+clearDataConfig.getName()+"执行完毕，执行结果："+(result.isSuccess()?"成功":"失败");
                    detailLog = result.getMessage();
                }else{
                    MongodbProp mongodbProp = mongodbPropService.getById(jobInfo.getConfigId());
                    String mongodbAddress = mongodbProp.getIp()+":"+mongodbProp.getPort();
                    handleMsgBuilder.append("mongodb备份："+mongodbAddress+"<br/>");
                    taskLog.append("mongodb备份任务执行开始："+DateUtil.format(start, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    MongoBackupResult mongoBackupResult = mongodbPropService.backup(mongodbProp);
                    Date end = new Date();
                    taskLog.append("mongodb备份任务执行结束："+DateUtil.format(end, DatePattern.NORM_DATETIME_PATTERN)+"<br/>");
                    long ms = DateUtil.betweenMs(start,end);
                    taskLog.append("执行耗时："+ms+"ms<br/>");
                    taskLog.append("执行结果："+(mongoBackupResult.isSuccess()?"<span style=\"color:green\">成功</span>":"<span style=\"color:red\">失败</span>")+"<br/>");
                    executeResult = mongoBackupResult.isSuccess();
                    if(executeResult){
                        taskLog.append("生成备份文件路径："+mongoBackupResult.getBackupDir()+"<br/>");
                    }
                    jobLog.setHandleCode(executeResult?HandleEnum.SUCCESS.getCode():HandleEnum.FAIL.getCode());
                    smsMsg = "mongodb备份任务："+mongodbAddress+"执行完毕，执行结果："+(mongoBackupResult.isSuccess()?"成功":"失败");
                    detailLog = mongoBackupResult.getMsg();
                    detailLog = detailLog.replaceAll("\n","<br/>");
                }
//                StringBuilder smssendBuilder = new StringBuilder();
//                StringBuilder sendmailBuilder = new  StringBuilder();
                doManagerSmsNotice(executeResult,smsMsg);
//                doNotice(propId,smsMsg,jobName,taskLog.toString(),detailLog,smssendBuilder,sendmailBuilder);
                handleMsgBuilder.append(taskLog);
//                handleMsgBuilder.append("短信发送情况：")
//                        .append(smssendBuilder);
//                handleMsgBuilder.append("邮件发送情况：")
//                        .append(sendmailBuilder.toString());
                handleMsgBuilder.append("执行日志明细：<br/>"+detailLog);
                jobLog.setEndDate(new Date());
                jobLog.setExecuteParam(jobInfo.getParam());
                jobLog.setHandleMsg(handleMsgBuilder.toString());
                jobLogMapper.updateById(jobLog);
            }catch (Exception e){
                log.error("调用出现异常：详情日志：",e);
                sb.append("调用出现异常：详情日志："+ ExceptionUtil.getMessage(e));
                jobLog.setHandleCode(HandleEnum.FAIL.getCode());
                jobLog.setEndDate(new Date());
                jobLog.setHandleMsg(sb.toString());
                jobLogMapper.updateById(jobLog);
            }
        });
    }
    private void doManagerSmsNotice(boolean executeResult,String smsMsg){
        if(!executeResult){
            List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.SUPER_ADMIN_ROLE);
            for(SysUser sysUser:sysUserList){
                if(StringUtils.isNotBlank(sysUser.getPhone())){
                    commonService.sendSms(sysUser.getPhone(),smsMsg);
                }
            }
        }

    }

    private void doNotice(Long propId,String smsMsg,String jobName,String taskLog,String detailLog,
                          StringBuilder smssendBuilder,StringBuilder sendmailBuilder){
        List<SysUser> smserList = new ArrayList<>();
        List<SysUser> mailerList = new ArrayList<>();
        getSmserAndMailer(propId,smserList,mailerList);
        sendSms(smserList,smsMsg,smssendBuilder);
        sendMail(mailerList,jobName,detailLog,taskLog,sendmailBuilder);

    }
    private void sendMail(List<SysUser> mailerList,String jobName,String detailLog,String taskLog,StringBuilder sendmailBuilder){
        String subject = jobName+"执行情况";
        String text = taskLog+"<br/>"+"执行日志明细：<br/>"+detailLog;
        if(mailerList.isEmpty()){
            sendmailBuilder.append("无<br/>");
            return ;
        }
        StringBuilder smedMailStr = new StringBuilder();
        StringBuilder smedMailAlias = new StringBuilder();
        for(SysUser sysUser:mailerList){
            smedMailStr.append(sysUser.getEmail()).append(",");
            smedMailAlias.append(sysUser.getRealname()).append("(").append(sysUser.getEmail()).append(")").append(",");
        }
        String smedMailAliasStr = smedMailAlias.substring(0,smedMailAlias.length()-1);
        try{
            commonService.sendMail(smedMailStr.substring(0,smedMailStr.length()-1),subject,text,null,null);
            sendmailBuilder.append("<span style=\"color:green\">联系人："+smedMailAliasStr+" 邮件发送成功</span><br/>");
        }catch (Exception e){
            sendmailBuilder.append("<span style=\"color:red\">联系人："+smedMailAliasStr+" 邮件发送失败</span>，异常日志<br/>"+e.toString()+"<br/>");
        }
    }


    private void sendSms(List<SysUser> smserList,String content,StringBuilder sendBuilder){
        if(smserList.isEmpty()){
            sendBuilder.append("无<br/>");
            return ;
        }
        for(SysUser smser:smserList){
            boolean success = commonService.sendSms(smser.getPhone(),content);
            if(success){
                sendBuilder.append("<span style=\"color:green\">").append(smser.getRealname())
                        .append("(").append(smser.getPhone()).append(")").append("发送短信成功</span><br/>");
            }else{
                sendBuilder.append("<span style=\"color:red\">").append(smser.getRealname())
                        .append("(").append(smser.getPhone()).append(")").append("发送短信失败</span><br/>");
            }
        }
    }


    private void getSmserAndMailer(Long propId,List<SysUser> smser ,List<SysUser> mailer ){
        DatasourceProp prop = datasourcePropMapper.selectById(propId);
        Business business = businessMapper.selectById(prop.getBusinessId());
        for(String userId:business.getUserId().split(",")){
            SysUser sysUser = sysUserMapper.selectById(userId);
            NoticeEnum noticeEnum = NoticeEnum.getNoticeEnum(sysUser.getNoticeType());
            if(noticeEnum==NoticeEnum.ALL||noticeEnum==NoticeEnum.MAIL){
                mailer.add(sysUser);
            }
            if(noticeEnum==NoticeEnum.ALL||noticeEnum==NoticeEnum.SMS){
                smser.add(sysUser);
            }
        }

    }


}
