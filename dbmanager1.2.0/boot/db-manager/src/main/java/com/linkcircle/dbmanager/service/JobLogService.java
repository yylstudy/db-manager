package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.JobLog;

import java.util.Map;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface JobLogService extends IService<JobLog> {
    IPage<JobLog> queryPage(Page<JobLog> page, JobLog jobLog);

    /**
     * 任务类型为数据清理时，查询备份的ibd和frm
     * @param page
     * @param id
     * @return
     */
    IPage<Map<String,String>> getIbdFrm(Page<Map<String, String>> page, Long id);

}
