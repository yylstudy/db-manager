package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.ClearDataConfig;

import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface ClearDataConfigService extends IService<ClearDataConfig> {
    /**
     * 测试数据库连接
     * @param id
     * @return
     */
    boolean testConnection(Long id);

    /**
     * 数据清理
     * @param clearDataConfig
     * @return
     */
    Result clearData(ClearDataConfig clearDataConfig,boolean checkTime);

    /**
     * 获取清理的表名
     * @param id
     * @return
     */
    List<String> getClearTableName(Long id);
    /**
     * 新增
     * @param clearDataConfig
     */
    void add(ClearDataConfig clearDataConfig);

    /**
     * 修改
     * @param clearDataConfig
     */
    void edit(ClearDataConfig clearDataConfig);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    IPage<ClearDataConfig> queryPage(Page<ClearDataConfig> page, ClearDataConfig clearDataConfig);

    Result testSsh(ClearDataConfig clearDataConfig);

    Result testmysql(ClearDataConfig clearDataConfig);

    long countByPropId(long propId);

}
