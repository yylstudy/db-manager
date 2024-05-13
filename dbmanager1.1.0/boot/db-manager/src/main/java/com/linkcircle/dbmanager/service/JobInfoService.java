package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.ComputerRoom;
import com.linkcircle.dbmanager.entity.JobInfo;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface JobInfoService extends IService<JobInfo> {
    IPage<JobInfo> queryPage(Page<JobInfo> page, JobInfo jobInfo);

    /**
     * 启动、停止任务
     * @param id
     * @param triggerStatus
     */
    void changeStatus(Long id,Integer triggerStatus);

    /**
     * 删除任务
     * @param id
     * @return
     */
    Result<?> delete(String id);

    ComputerRoom getComputerRoom(Long id);
}
