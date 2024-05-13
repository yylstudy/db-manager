package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.JobInfoHis;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface JobInfoHisService extends IService<JobInfoHis> {
    IPage<JobInfoHis> queryPage(Page<JobInfoHis> page, JobInfoHis jobInfoHis);

    /**
     * 恢复删除的历史任务
     * @param id
     * @return
     */
    Result recovery(Long id);
}
