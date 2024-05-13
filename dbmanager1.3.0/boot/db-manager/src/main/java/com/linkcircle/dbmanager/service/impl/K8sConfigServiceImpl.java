package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.config.K8sClientManager;
import com.linkcircle.dbmanager.entity.K8sConfig;
import com.linkcircle.dbmanager.mapper.K8sConfigMapper;
import com.linkcircle.dbmanager.service.K8sConfigService;
import com.linkcircle.fileservice.entity.Attachment;
import com.linkcircle.fileservice.service.AttachmentService;
import com.linkcircle.fileservice.util.IDGenerator;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
@Slf4j
public class K8sConfigServiceImpl extends ServiceImpl<K8sConfigMapper, K8sConfig> implements K8sConfigService {
    @Autowired
    private AttachmentService attachmentService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(K8sConfig k8sConfig) {
        try{
            k8sConfig.setId(IDGenerator.nextId());
            Attachment attachment = attachmentService.findById(k8sConfig.getAttachementIds().get(0));
            ApiClient client = Config.fromConfig(attachment.getFullPath());
            this.save(k8sConfig);
            attachmentService.addRelationShip(k8sConfig.getAttachementIds(),String.valueOf(k8sConfig.getId()));
            K8sClientManager.put(k8sConfig.getId(),client);
        }catch (Exception e){
            log.error("添加k8s配置失败，请检查k8s配置是否正确",e);
            throw new BusinessException("添加k8s配置失败，请联系管理员");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(K8sConfig k8sConfig) {
        try{
            Attachment attachment = attachmentService.findById(k8sConfig.getAttachementIds().get(0));
            ApiClient client = Config.fromConfig(attachment.getFullPath());
            this.updateById(k8sConfig);
            attachmentService.updateRelationShip(k8sConfig.getAttachementIds(),String.valueOf(k8sConfig.getId()),"1001");
            K8sClientManager.put(k8sConfig.getId(),client);
        }catch (Exception e){
            log.error("更新k8s配置失败，请检查k8s配置是否正确",e);
            throw new BusinessException("更新k8s配置失败，请联系管理员");
        }
    }

}
