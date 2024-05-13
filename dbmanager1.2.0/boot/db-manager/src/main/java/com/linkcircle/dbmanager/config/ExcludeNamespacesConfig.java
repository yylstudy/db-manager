package com.linkcircle.dbmanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/9/21 16:25
 */
@Data
@ConfigurationProperties(prefix = "k8s")
@Component
public class ExcludeNamespacesConfig {
    private List<String> excludeNamespaces;
}
