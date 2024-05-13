package com.linkcircle.dbmanager.job;

import com.linkcircle.boot.service.CommonService;
import com.linkcircle.boot.thread.ThreadPoolFactory;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.entity.ComputerRoom;
import com.linkcircle.dbmanager.service.ComputerRoomService;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

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


}
