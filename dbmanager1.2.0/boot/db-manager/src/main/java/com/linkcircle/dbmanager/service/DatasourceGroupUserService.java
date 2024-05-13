package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.DatasourceGroupUser;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/22 17:21
 */

public interface DatasourceGroupUserService extends IService<DatasourceGroupUser> {
    DatasourceGroupUser findByGroupIdAndUser(long groupId,String username);
}
