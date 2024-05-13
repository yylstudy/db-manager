package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.common.MongoBackupResult;
import com.linkcircle.dbmanager.entity.MongodbProp;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface MongodbPropService extends IService<MongodbProp> {
    IPage<MongodbProp> queryPage(Page<MongodbProp> page, MongodbProp mongodbProp);
    MongoBackupResult backup(MongodbProp mongodbProp);
}
