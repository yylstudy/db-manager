package com.linkcircle.dbmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.dbmanager.entity.DatasourceResetPasswordHis;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/5/11 10:10
 */

public interface DatasourceResetPasswordHisMapper extends BaseMapper<DatasourceResetPasswordHis> {

    IPage<DatasourceResetPasswordHis> queryPage(Page<DatasourceResetPasswordHis> page, DatasourceResetPasswordHis his);

}
