package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.common.DbUser;
import com.linkcircle.dbmanager.common.GroupUserDto;
import com.linkcircle.dbmanager.common.ResetGroupUserDto;
import com.linkcircle.dbmanager.entity.DatasourceProp;
import com.linkcircle.dbmanager.entity.MysqlUser;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface DatasourcePropService extends IService<DatasourceProp> {
    Result<?> userList(Long id);
    Result<?> addGroupUser(GroupUserDto groupUserDto);
    Result<?> deleteGroupUser(GroupUserDto groupUserDto);
    Result<?> getDatabase(MysqlUser mysqlUser);
    List<DbUser> getGroupUser(Long groupId,String username,boolean queryLocal);
    Result<?> resetGroupUserPassword(ResetGroupUserDto resetGroupUserDto);
    Result<?> grantGroupPrivileges(GroupUserDto groupUserDto);
    Result<?> delete(String id);
    Result<?> add(DatasourceProp datasourceProp);
    Result<?> edit(DatasourceProp datasourceProp);
    Result<?> getMysqlUserByLevel();
    IPage<DatasourceProp> queryPage(Page<DatasourceProp> page, DatasourceProp datasourceProp);
    void sendAllPasswordEmail(String emails);
    Result<?> getRealDatabase(Long mysqlUserId);
    Result<?> getRealDatabaseByPropId(Long propId);
    List<DatasourceProp> findByGroupId(Long groupId);
    Result userGroupList(Long groupId);
    Result resetGroupPwd(GroupUserDto groupUserDto);
    Result<?> getNamespace(Long k8sConfigId);
    Result<?> getPod(Long k8sConfigId,String namespace);
}
