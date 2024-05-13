package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.DatasourceResetPasswordHis;
import com.linkcircle.dbmanager.mapper.DatasourceResetPasswordHisMapper;
import com.linkcircle.dbmanager.service.DatasourceResetPasswordHisService;
import org.springframework.stereotype.Service;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/5/11 10:11
 */
@Service
public class DatasourceResetPasswordHisServiceImpl extends ServiceImpl<DatasourceResetPasswordHisMapper, DatasourceResetPasswordHis> implements DatasourceResetPasswordHisService {
    @Override
    public IPage<DatasourceResetPasswordHis> queryPage(Page<DatasourceResetPasswordHis> page, DatasourceResetPasswordHis his) {
        return this.baseMapper.queryPage(page,his);
    }
}
