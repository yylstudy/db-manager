package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.TableNameRule;
import com.linkcircle.dbmanager.mapper.TableNameRuleMapper;
import com.linkcircle.dbmanager.service.TableNameRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/12 9:48
 */
@Service
public class TableNameRuleServiceImpl extends ServiceImpl<TableNameRuleMapper, TableNameRule> implements TableNameRuleService {
    @Override
    public List<TableNameRule> findByClearDataConfigId(Long clearDataConfigId) {
        LambdaQueryWrapper<TableNameRule> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TableNameRule::getClearDataConfigId,clearDataConfigId);
        return this.list(wrapper);
    }
}
