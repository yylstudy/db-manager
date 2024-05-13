package com.linkcircle.dbmanager.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.cron.CronExpression;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.service.JobInfoService;
import com.linkcircle.dbmanager.thread.JobTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/jobinfo")
public class JobInfoController {
    @Autowired
    private JobInfoService jobInfoService;
    @Autowired
    private JobTriggerService jobTriggerService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<JobInfo>> queryPageList(JobInfo jobInfo, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<JobInfo> page = new Page<JobInfo>(pageNo, pageSize);
        IPage<JobInfo> pageList = jobInfoService.queryPage(page, jobInfo);
        return Result.OK(pageList);
    }
    @PostMapping(value = "executeOneTime")
    public Result executeOneTime(@RequestBody JobInfo jobInfo) {
        jobTriggerService.addTrigger(jobInfo,false);
        return Result.OK();
    }
    @PostMapping(value = "changeStatus")
    public Result changeStatus(@RequestParam Long id,@RequestParam Integer triggerStatus) {
        jobInfoService.changeStatus(id,triggerStatus);
        return Result.OK();
    }

    @RequestMapping("/nextTriggerTime")
    public Result nextTriggerTime(@RequestParam  Long id) {
        List<String> result = new ArrayList<>();
        try {
            JobInfo jobInfo = jobInfoService.getById(id);
            CronExpression cronExpression = new CronExpression(jobInfo.getJobCron());
            Date lastTime = new Date();
            for (int i = 0; i < 5; i++) {
                lastTime = cronExpression.getNextValidTimeAfter(lastTime);
                if (lastTime != null) {
                    result.add(DateUtil.formatDateTime(lastTime));
                } else {
                    break;
                }
            }
        } catch (ParseException e) {
            return Result.error("无效的cron表达式");
        }
        return Result.OK(result);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody JobInfo jobInfo) {
        jobInfo.setTriggerStatus(0);
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return Result.error("无效的cron");
        }
        jobInfoService.save(jobInfo);
        return Result.OK();
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result edit(@RequestBody JobInfo jobInfo) {
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return Result.error("无效的cron");
        }
        JobInfo dbInfo = jobInfoService.getById(jobInfo.getId());
        //cron修改，则重置triggerNextTime，不然会导致下次执行时间还是之前cron的配置
        if(!StringUtils.equals(dbInfo.getJobCron(),jobInfo.getJobCron())){
            jobInfo.setTriggerNextTime(0L);
        }
        jobInfoService.updateById(jobInfo);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        return jobInfoService.delete(id);
    }

}
