package com.linkcircle.boot.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.linkcircle.boot.service.ApplicationContextSupport;
import com.linkcircle.boot.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/18 17:03
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
    @Value("${msg.msgAccount}")
    private  String msgAccount;
    @Value("${msg.sign}")
    private  String msgSign;
    @Value("${msg.msgPwd}")
    private  String msgPwd;
    @Value("${msg.smsUrl}")
    private  String smsUrl;
    @Autowired
    private MailProperties mailProperties;
    @Override
    public  void sendMail(String mailers, String subject, String text, Resource fileResource,String attachmentName) throws Exception{
        JavaMailSenderImpl mailSender = ApplicationContextSupport.getBean(JavaMailSenderImpl.class);
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(),true);
        messageHelper.setFrom(mailProperties.getUsername());
        messageHelper.setTo(mailers.split(","));
        if(fileResource!=null){
            messageHelper.addAttachment(attachmentName, fileResource);
        }
        messageHelper.setSubject(subject);
        messageHelper.setText(text,true);
        mailSender.send(messageHelper.getMimeMessage());
    }
    @Override
    public boolean sendSms(String phone,String content) {
        try{
            HttpHeaders httpHeaders = new HttpHeaders();
            String messagetag ="99";
            String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
            httpHeaders.set("Content-Type",contentType);
            StringBuilder sb = new StringBuilder();
            sb.append("userid="+messagetag);
            sb.append("&account="+msgAccount);
            sb.append("&password="+msgPwd);
            sb.append("&content="+msgSign+content);
            sb.append("&mobile="+phone);
            sb.append("&extno=106907891234");
            sb.append("&action=send");
            sb.append("&rt=json");
            byte[] headerBytes = sb.toString().getBytes(Charset.forName("utf-8"));
            HttpEntity httpEntity = new HttpEntity(headerBytes,httpHeaders);
            JSONObject jsonObject = ApplicationContextSupport.getBean(RestTemplate.class).postForObject(smsUrl,httpEntity,JSONObject.class);
            log.info("send sms result:{}",jsonObject);
            Object status = jsonObject.get("status");
            if(null != status && status.toString().equals("0")){
                return true;
            }
        }catch (Exception e){
            log.error("send sms error",e);
        }
        return false;
    }



}
