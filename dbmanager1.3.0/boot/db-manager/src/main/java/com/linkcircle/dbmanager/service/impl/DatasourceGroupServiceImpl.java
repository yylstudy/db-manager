package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.DatasourceGroup;
import com.linkcircle.dbmanager.entity.DatasourceGroupUser;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.mapper.DatasourceGroupMapper;
import com.linkcircle.dbmanager.service.DatasourceGroupService;
import com.linkcircle.dbmanager.service.DatasourceGroupUserService;
import com.linkcircle.dbmanager.service.DatasourcePropService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/19 16:33
 */
@Service
@Slf4j
public class DatasourceGroupServiceImpl extends ServiceImpl<DatasourceGroupMapper, DatasourceGroup> implements DatasourceGroupService {

    @Autowired
    private DatasourcePropService datasourcePropService;
    @Autowired
    private DatasourceGroupUserService datasourceGroupUserService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result add(DatasourceGroup datasourceGroup) {
        List<Long> datasourcePropsIds = Arrays.stream(datasourceGroup.getDatasourcePropsIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
        boolean allNull =  datasourcePropService.listByIds(datasourcePropsIds).stream()
                .allMatch(prop->prop.getGroupId()==null);
        if(!allNull){
            return Result.error("数据源已被其他组使用");
        }
        this.save(datasourceGroup);
        for (Long datasourcePropsId : datasourcePropsIds) {
            DatasourceProp datasourceProp = datasourcePropService.getById(datasourcePropsId);
            datasourceProp.setGroupId(datasourceGroup.getId());
            datasourcePropService.updateById(datasourceProp);
        }
        return Result.OK();
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result edit(DatasourceGroup datasourceGroup) {
        List<Long> datasourcePropsIds = Arrays.stream(datasourceGroup.getDatasourcePropsIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
        List<DatasourceProp> list =  datasourcePropService.listByIds(datasourcePropsIds).stream().filter(prop->prop.getGroupId()!=null)
                .collect(Collectors.toList());
        if(!list.isEmpty()){
            List<Long> dbList = list.stream().map(DatasourceProp::getGroupId).distinct().collect(Collectors.toList());
            if(dbList.size()!=1||!dbList.get(0).equals(datasourceGroup.getId())){
                return Result.error("数据源已被其他组使用");
            }
        }
        this.updateById(datasourceGroup);
        List<DatasourceProp> dbList = datasourcePropService.findByGroupId(datasourceGroup.getId());
        List<Long> deletList = dbList.stream().filter(prop->!datasourcePropsIds.contains(prop.getId())).map(DatasourceProp::getId).collect(Collectors.toList());
        for (Long datasourcePropsId : deletList) {
            DatasourceProp datasourceProp = datasourcePropService.getById(datasourcePropsId);
            datasourceProp.setGroupId(null);
            datasourcePropService.updateById(datasourceProp);
        }
        for (Long datasourcePropsId : datasourcePropsIds) {
            log.info("datasourcePropsId:{}",datasourcePropsId);
            DatasourceProp datasourceProp = datasourcePropService.getById(datasourcePropsId);
            datasourceProp.setGroupId(datasourceGroup.getId());
            datasourcePropService.updateById(datasourceProp);
        }
        return Result.OK();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(Long id) {
        List<DatasourceProp> datasourceProps = datasourcePropService.findByGroupId(id);
        for (DatasourceProp datasourceProp : datasourceProps) {
            datasourceProp.setGroupId(null);
            datasourcePropService.updateById(datasourceProp);
        }
        LambdaQueryWrapper<DatasourceGroupUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DatasourceGroupUser::getGroupId,id);
        datasourceGroupUserService.remove(wrapper);
        this.removeById(id);
        return Result.OK();
    }
}
