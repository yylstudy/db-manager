package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.TableNameRule;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/12 9:47
 */
public interface TableNameRuleService extends IService<TableNameRule> {
    List<TableNameRule> findByClearDataConfigId(Long clearDataConfigId);
}
