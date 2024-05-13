package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.MysqlUser;
import com.linkcircle.dbmanager.mapper.MysqlUserMapper;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import com.linkcircle.dbmanager.service.MysqlUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/5 11:28
 */
@Service
public class MysqlUserServiceImpl extends ServiceImpl<MysqlUserMapper, MysqlUser> implements MysqlUserService {
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Override
    public MysqlUser getMysqlUser(Long porpId, String username){
        LambdaQueryWrapper<MysqlUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MysqlUser::getPropId,porpId);
        wrapper.eq(MysqlUser::getUsername,username);
        return this.getOne(wrapper);
    }
    @Override
    public List<MysqlUser> getMysqlUser(Long porpId){
        LambdaQueryWrapper<MysqlUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MysqlUser::getPropId,porpId);
        return this.list(wrapper);
    }

    @Override
    public MysqlUser getFullMysqlUser(Long id) {
        MysqlUser mysqlUser = this.getById(id);
        DatasourceProp datasourceProp = datasourcePropService.getById(mysqlUser.getPropId());
        mysqlUser.setIp(datasourceProp.getIp());
        mysqlUser.setPort(datasourceProp.getPort());
        return mysqlUser;
    }

}
