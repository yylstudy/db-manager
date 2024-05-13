package com.linkcircle.dbmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.dbmanager.entity.DatasourceProp;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/2 14:23
 */
public interface DatasourcePropMapper extends BaseMapper<DatasourceProp> {
    IPage<DatasourceProp> queryPage(Page<DatasourceProp> page, DatasourceProp datasourceProp);
}
