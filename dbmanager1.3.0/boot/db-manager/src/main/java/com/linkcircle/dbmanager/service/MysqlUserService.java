package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.MysqlUser;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/5 11:28
 */
public interface MysqlUserService extends IService<MysqlUser> {
    MysqlUser getMysqlUser(Long porpId, String username);
    List<MysqlUser> getMysqlUser(Long porpId);
    MysqlUser getFullMysqlUser(Long id);
}
