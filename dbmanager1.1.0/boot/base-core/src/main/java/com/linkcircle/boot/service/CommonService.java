package com.linkcircle.boot.service;

import org.springframework.core.io.Resource;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/18 17:03
 */
public interface CommonService {
    void sendMail(String mailers,String subject,String text, Resource fileResource,String attachmentName) throws Exception;

    boolean sendSms(String phone,String content);



}
