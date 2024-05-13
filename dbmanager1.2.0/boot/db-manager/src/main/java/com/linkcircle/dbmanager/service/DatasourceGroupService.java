package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.DatasourceGroup;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/19 16:33
 */

public interface DatasourceGroupService extends IService<DatasourceGroup> {

    Result add(DatasourceGroup datasourceGroup);

    Result edit(DatasourceGroup datasourceGroup);

    Result delete(Long id);

}
