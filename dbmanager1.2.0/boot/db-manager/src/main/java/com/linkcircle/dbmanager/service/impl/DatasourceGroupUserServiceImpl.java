package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.DatasourceGroupUser;
import com.linkcircle.dbmanager.mapper.DatasourceGroupUserMapper;
import com.linkcircle.dbmanager.service.DatasourceGroupUserService;
import org.springframework.stereotype.Service;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/22 17:21
 */
@Service
public class DatasourceGroupUserServiceImpl extends ServiceImpl<DatasourceGroupUserMapper, DatasourceGroupUser> implements DatasourceGroupUserService {
    @Override
    public DatasourceGroupUser findByGroupIdAndUser(long groupId, String username) {
        LambdaQueryWrapper<DatasourceGroupUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DatasourceGroupUser::getGroupId,groupId);
        wrapper.eq(DatasourceGroupUser::getUsername,username);
        return this.getOne(wrapper);
    }
}
