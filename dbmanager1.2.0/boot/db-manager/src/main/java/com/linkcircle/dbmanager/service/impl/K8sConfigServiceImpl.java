package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.config.K8sClientManager;
import com.linkcircle.dbmanager.entity.K8sConfig;
import com.linkcircle.dbmanager.mapper.K8sConfigMapper;
import com.linkcircle.dbmanager.service.K8sConfigService;
import com.linkcircle.dbmanager.util.SshUtil;
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
            checkNfsDir(k8sConfig,k8sConfig.getNfsBaseDir());
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
            checkNfsDir(k8sConfig,k8sConfig.getNfsBaseDir());
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

    private void checkNfsDir(K8sConfig k8sConfig,String dir){
        Session session = null;
        try{
            session = SshUtil.getSession(k8sConfig.getNfsSshIp(),k8sConfig.getNfsSshPort(), k8sConfig.getNfsSshUser(),
                    k8sConfig.getNfsSshPassword());
            Integer execStatus = SshUtil.getExecStatus(session,"cd "+dir);
            if(execStatus==null||execStatus!=0){
                throw new BusinessException("nfs目录："+dir+"不存在，请检查配置");
            }
        }catch (Exception e){
            log.error("nfs ssh配置错误，请检查",e);
            throw new BusinessException("nfs ssh配置错误，请检查");
        }finally {
            if(session!=null){
                session.disconnect();
            }
        }
    }

}
