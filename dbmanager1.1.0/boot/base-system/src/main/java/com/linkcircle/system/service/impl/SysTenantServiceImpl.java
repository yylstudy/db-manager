package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.system.entity.SysTenant;
import com.linkcircle.system.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import com.linkcircle.boot.common.constant.CommonConstant;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.system.mapper.SysTenantMapper;
import com.linkcircle.system.service.ISysTenantService;
import com.linkcircle.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service("sysTenantServiceImpl")
@Slf4j
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    @Autowired
    ISysUserService userService;

    @Override
    public List<SysTenant> queryEffectiveTenant(Collection<String> idList) {
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysTenant::getId, idList);
        queryWrapper.eq(SysTenant::getStatus, CommonConstant.STATUS_1);
        //此处查询忽略时间条件
        return super.list(queryWrapper);
    }

    @Override
    public int countUserLinkTenant(String id) {
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getRelTenantIds, id);
        userQueryWrapper.or().like(SysUser::getRelTenantIds, "%," + id);
        userQueryWrapper.or().like(SysUser::getRelTenantIds, id + ",%");
        userQueryWrapper.or().like(SysUser::getRelTenantIds, "%," + id + ",%");
        // 查找出已被关联的用户数量
        return userService.count(userQueryWrapper);
    }

    @Override
    public boolean removeTenantById(String id) {
        // 查找出已被关联的用户数量
        int userCount = this.countUserLinkTenant(id);
        if (userCount > 0) {
            throw new BusinessException("该租户已被引用，无法删除！");
        }
        return super.removeById(id);
    }

}