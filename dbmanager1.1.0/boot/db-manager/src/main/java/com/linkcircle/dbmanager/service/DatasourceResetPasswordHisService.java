package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.DatasourceResetPasswordHis;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/5/11 10:11
 */

public interface DatasourceResetPasswordHisService extends IService<DatasourceResetPasswordHis> {
    IPage<DatasourceResetPasswordHis> queryPage(Page<DatasourceResetPasswordHis> page, DatasourceResetPasswordHis his);
}
