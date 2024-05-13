package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.entity.JobInfoHis;
import com.linkcircle.dbmanager.mapper.JobInfoHisMapper;
import com.linkcircle.dbmanager.service.JobInfoHisService;
import com.linkcircle.dbmanager.service.JobInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
public class JobInfoHisServiceImpl extends ServiceImpl<JobInfoHisMapper, JobInfoHis> implements JobInfoHisService {
    @Autowired
    private JobInfoService jobInfoService;
    @Override
    public IPage<JobInfoHis> queryPage(Page<JobInfoHis> page, JobInfoHis jobInfoHis) {
        return this.baseMapper.queryPage(page,jobInfoHis);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result recovery(Long id) {
        JobInfoHis jobInfoHis = this.getById(id);
        JobInfo jobInfo = new JobInfo();
        BeanUtils.copyProperties(jobInfoHis,jobInfo);
        jobInfoService.save(jobInfo);
        this.removeById(id);
        return Result.OK();
    }
}
