package com.linkcircle.dbmanager.config;

import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.boot.service.ApplicationContextSupport;
import com.linkcircle.fileservice.entity.Attachment;
import com.linkcircle.fileservice.service.AttachmentService;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/9/19 15:51
 */
@Component
public class K8sClientManager {
    private static Map<Long, ApiClient> map = new ConcurrentHashMap<>();

    public static void put(Long id,ApiClient apiClient){
        map.put(id,apiClient);
    }

    public static void delete(Long id){
        map.remove(id);
    }

    public static ApiClient get(Long id){
        return map.computeIfAbsent(id,key->{
            AttachmentService attachmentService = ApplicationContextSupport.getBean(AttachmentService.class);
            List<Attachment> list = attachmentService.selectByBusinessIdAndType(String.valueOf(id),"1001");
            if(list.isEmpty()){
                throw new BusinessException("未发现k8s配置文件");
            }
            try{
                ApiClient client = Config.fromConfig(list.get(0).getFullPath());
                return client;
            }catch (Exception e){
                throw new BusinessException("加载k8s配置文件失败，请检查");
            }
        });
    }
    public static CoreV1Api getCoreV1Api(Long id){
        CoreV1Api api = new CoreV1Api(get(id));
        return api;
    }

}
