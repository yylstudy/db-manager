package com.linkcircle.dbmanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.dbmanager.entity.K8sConfig;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:39
 **/
public interface K8sConfigService extends IService<K8sConfig> {
    void add(K8sConfig k8sConfig);
    void edit(K8sConfig user);
}
