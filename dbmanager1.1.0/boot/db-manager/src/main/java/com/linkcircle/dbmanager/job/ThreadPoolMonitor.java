package com.linkcircle.dbmanager.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.linkcircle.boot.service.CommonService;
import com.linkcircle.boot.thread.ThreadPoolFactory;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.HandleEnum;
import com.linkcircle.dbmanager.common.TaskTypeEnum;
import com.linkcircle.dbmanager.entity.ComputerRoom;
import com.linkcircle.dbmanager.entity.JobLog;
import com.linkcircle.dbmanager.service.ComputerRoomService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.dbmanager.service.JobLogService;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/21 16:19
 */
@Slf4j
@Component
public class ThreadPoolMonitor {
    @Autowired
    private CommonService commonService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ComputerRoomService computerRoomService;
    @Autowired
    private JobLogService jobLogService;
    @Value("${platform-name:}")
    private String platformName;
    @Autowired
    private DatasourcePropService datasourcePropService;

    @Scheduled(cron = "${monitorCron}")
    public void monitorTask(){
        Map<Long,ThreadPoolExecutor> computerRoomThreadPoolMap = ThreadPoolFactory.computerRoomThreadPoolMap;
        computerRoomThreadPoolMap.forEach((computerRoomId,poolExecutor)->{
            try{
                ComputerRoom computerRoom = computerRoomService.getById(computerRoomId);
                int activeCount = poolExecutor.getActiveCount();
                int queueSize = poolExecutor.getQueue().size();
                int taskNum = activeCount+queueSize;
                if(taskNum>0){
                    List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.SUPER_ADMIN_ROLE);
                    for(SysUser sysUser:sysUserList){
                        if(StringUtils.isNotBlank(sysUser.getPhone())){
                            commonService.sendSms(sysUser.getPhone(),computerRoom.getName()+"数据库备份、清理线程池还有"+taskNum+"个任务未完成，请检查！");
                        }
                    }
                    log.error(computerRoom.getName()+"数据库备份、清理线程池还有"+taskNum+"个任务未完成，请检查！");

                }else{
                    log.info(computerRoom.getName()+"线程池内任务全部执行完毕！");
                }
            }catch (Exception e){
                log.error("monitorTask error",e);
            }
        });

    }

    @Scheduled(cron = "${monitorCron}")
    public void reportTask(){
        PageDTO<JobLog> page = new PageDTO();
        page.setSize(10000);
        JobLog queryJobLog = new JobLog();
        String queryDateStart = DateUtil.offsetDay(new Date(),-1).toDateStr()+" 21:00:00";
        String queryDateEnd = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);
        queryJobLog.setQueryDateStart(queryDateStart);
        queryJobLog.setQueryDateEnd(queryDateEnd);
        List<JobLog> list = jobLogService.queryPage(page,queryJobLog).getRecords();
        list = list.stream().sorted((j1,j2)->{
            if(StringUtils.isEmpty(j1.getBusinessName())
                    ||StringUtils.isEmpty(j1.getTaskType())||StringUtils.isEmpty(j1.getComputerRoomName())){
                return -1;
            }
            int i = j1.getBusinessName().compareTo(j2.getBusinessName());
            if(i!=0){
                return i;
            }
            i = j1.getTaskType().compareTo(j2.getTaskType());
            if(i!=0){
                return i;
            }
            return j1.getComputerRoomName().compareTo(j2.getComputerRoomName());

        }).collect(Collectors.toList());
        int clearSuccess = 0;
        int clearFail = 0;
        int clearIng = 0;
        int backSuccess = 0;
        int backFail = 0;
        int backIng = 0;
        int mongoBackSuccess = 0;
        int mongoBackFail = 0;
        int mongoBackIng = 0;
        StringBuilder sb = new StringBuilder("");
        StringBuilder failureDetail = new StringBuilder("");
        StringBuilder tableHtml = new StringBuilder("<style>\n" +
                "            td,\n" +
                "            th,\n" +
                "            table {\n" +
                "                border: 1px solid #284;\n" +
                "            }\n" +
                "            table {\n" +
                "                border-spacing: 0;\n" +
                "            }\n" +
                "        </style><br/><table width=\"1200px\">");
        tableHtml.append("<tr>");
        tableHtml.append("<th>").append("业务名称").append("</th>");
        tableHtml.append("<th>").append("机房").append("</th>");
        tableHtml.append("<th>").append("任务类型").append("</th>");
        tableHtml.append("<th>").append("规则名称").append("</th>");
        tableHtml.append("<th>").append("数据源").append("</th>");
        tableHtml.append("<th>").append("执行开始时间").append("</th>");
        tableHtml.append("<th>").append("执行结束时间").append("</th>");
        tableHtml.append("<th>").append("执行结果").append("</th>").append("</tr>");

        for(JobLog jobLog:list){
            tableHtml.append("<tr>");
            tableHtml.append("<th>").append(jobLog.getBusinessName()).append("</th>");
            tableHtml.append("<th>").append(jobLog.getComputerRoomName()).append("</th>");
            tableHtml.append("<th>").append(TaskTypeEnum.getTaskTypeEnum(jobLog.getTaskType()).getDesc()).append("</th>");
            tableHtml.append("<th>").append(jobLog.getConfigName()).append("</th>");
            tableHtml.append("<th>").append(jobLog.getDatasourceName()).append("</th>");
            tableHtml.append("<th>").append(DateUtil.format(jobLog.getStartDate(),DatePattern.NORM_DATETIME_PATTERN)).append("</th>");
            tableHtml.append("<th>").append(jobLog.getEndDate()==null?"":DateUtil.format(jobLog.getEndDate(),DatePattern.NORM_DATETIME_PATTERN)).append("</th>");
            HandleEnum handleEnum = HandleEnum.getHandleEnum(jobLog.getHandleCode());
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.getTaskTypeEnum(jobLog.getTaskType());
            if(handleEnum==HandleEnum.FAIL){
                tableHtml.append("<th style=\"color:red\">").append("失败").append("</th>");
                sb.append(jobLog.getProBusName()).append("-").append(jobLog.getDatasourceName()).append(",");
                failureDetail.append("<span style=\"color:red\">数据源："+jobLog.getDatasourceName()).append("执行").append(taskTypeEnum.getDesc()).append("失败，详细信息：</span>").append("<br/>")
                        .append(jobLogService.getById(jobLog.getId()).getHandleMsg()).append("<br/><br/><br/>");
                if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
                    backFail++;
                }else if(taskTypeEnum==TaskTypeEnum.CLEAR_DATA){
                    clearFail++;
                }else if(taskTypeEnum==TaskTypeEnum.MONGODB_BACK){
                    mongoBackFail++;
                }
            }else if(handleEnum==HandleEnum.SUCCESS){
                tableHtml.append("<th style=\"color:green\">").append("成功").append("</th>");
                if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
                    backSuccess++;
                }else if(taskTypeEnum==TaskTypeEnum.CLEAR_DATA){
                    clearSuccess++;
                }else if(taskTypeEnum==TaskTypeEnum.MONGODB_BACK){
                    mongoBackSuccess++;
                }
            }else if(handleEnum==HandleEnum.UNDO){
                tableHtml.append("<th style=\"color:green\">").append("执行中").append("</th>");
                sb.append(jobLog.getDatasourceName()).append(",");
                if(taskTypeEnum==TaskTypeEnum.BACK_MYSQL){
                    backIng++;
                }else if(taskTypeEnum==TaskTypeEnum.CLEAR_DATA){
                    clearIng++;
                }else if(taskTypeEnum==TaskTypeEnum.MONGODB_BACK){
                    mongoBackIng++;
                }
            }
            tableHtml.append("</tr>");
        }
        tableHtml.append("</table>");
        String failDatasoueceIp = sb.toString();
        if(StringUtils.isNotBlank(failDatasoueceIp)){
            failDatasoueceIp = failDatasoueceIp.substring(0,failDatasoueceIp.length()-1);
        }
        String dateStr = DateUtil.offsetDay(new Date(),-1).toDateStr();
        String smsMessage = platformName+"数据库运维巡检日报："+dateStr+";mysql数据库备份执行情况：成功"+backSuccess
                +"台，执行中"+backIng
                +"台，失败"+
                backFail+"台;mongodb数据库备份执行情况：成功"+mongoBackSuccess
                +"台，执行中"+mongoBackIng
                +"台，失败"+
                mongoBackFail+"台;mysql数据库清理情况：成功"+
                clearSuccess+"台，执行中"+
                clearIng+"台，失败"+clearFail+"台；失败任务："+failDatasoueceIp+"；";
        String emailMessage = smsMessage+"<br/><br/><br/>执行明细如下<br/><br/>"+tableHtml;
        emailMessage += "<br/><br/><br/>失败明细：<br/>"+failureDetail.toString();
        List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.MONITOR_ROLE);
        log.info("sysUserList:{}",sysUserList);
        String subject = "数据库运维巡检日报";
        for(SysUser sysUser:sysUserList){
            if(StringUtils.isNotBlank(sysUser.getPhone())){
                commonService.sendSms(sysUser.getPhone(),smsMessage);
            }
            if(StringUtils.isNotBlank(sysUser.getEmail())){
                try{
                    commonService.sendMail(sysUser.getEmail(),subject,emailMessage,null,null);
                }catch (Exception e){
                    log.error("sendMail to"+sysUser.getEmail()+" failure",e);
                }
            }
        }
    }


    @Scheduled(cron = "0 0 9 1 * ?")
    public void sendPassword(){
        log.info("-------------发送所有数据库密码开始-------------------");
        List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.PWD_CHANGE_ROLE);
        String emails = sysUserList.stream().map(SysUser::getEmail).collect(Collectors.joining(","));
        log.info("-------------发送所有数据库密码至邮箱：{}-------------------",emails);
        datasourcePropService.sendAllPasswordEmail(emails);
        log.info("-------------发送所有数据库密码结束-------------------");
    }


}
