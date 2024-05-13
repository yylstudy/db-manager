package com.linkcircle.dbmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.dbmanager.entity.ClearDataConfig;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:38
 **/
public interface ClearDataConfigMapper extends BaseMapper<ClearDataConfig> {
    IPage<ClearDataConfig> queryPage(Page<ClearDataConfig> page, ClearDataConfig clearDataConfig);
    long countByPropId(long propId);
}
