package com.linkcircle.dbmanager.util;

import com.alibaba.druid.filter.config.ConfigTools;
import com.linkcircle.boot.common.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/2 14:56
 */
@Component
public class PwdUtil {

    private static String defaultPrivateKey;

    private static String defaultPublicKey;
    @Value("${defaultPrivateKey}")
    public  void setDefaultPrivateKey(String defaultPrivateKey) {
        PwdUtil.defaultPrivateKey = defaultPrivateKey;
    }
    @Value("${defaultPublicKey}")
    public  void setDefaultPublicKey(String defaultPublicKey) {
        PwdUtil.defaultPublicKey = defaultPublicKey;
    }

    public static String encryptPwd(String password){
        try{
            return ConfigTools.encrypt(defaultPrivateKey,password);
        }catch (Exception e){
            throw new BusinessException("密码加密失败");
        }
    }

    public static String decryptPwd(String password){
        return decryptPwd(defaultPublicKey,password);
    }
    public static String decryptPwd(String publicKey,String password){
        try{
            return ConfigTools.decrypt(publicKey,password);
        }catch (Exception e){
            throw new BusinessException("密码解密失败");
        }
    }

    public static void main(String[] args) throws Exception{
        String pwd = PwdUtil.definedPWDRoles(10,10);
        System.out.println(pwd);
    }

    public static String definedPWDRoles(int minlen,int maxlen){
        Random r = new Random();
        StringBuilder sbPWD = new StringBuilder();
//		密码范围
        String Wordstr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String Numstr = "1234567890";
        String Spsstr = "@#";
//		密码长度
        int len = r.nextInt(maxlen-minlen+1)+minlen;
//		密码类型随机
        List<Object> dic = new ArrayList<>();
        dic.add(Wordstr);
        dic.add(Numstr);
        dic.add(Spsstr);
        int restLen = len;
        for(int i=0,j=dic.size();i<j;i++){
            int lenThis=r.nextInt(restLen-(j-i-1))+1;
            restLen -= lenThis;
            int index = r.nextInt(dic.size()-1);
            sbPWD.append(randomPWD(dic.get(index).toString().toCharArray(), lenThis));
            dic.remove(index);
            if(j-i==2){
                sbPWD.append(randomPWD(dic.get(0).toString().toCharArray(), restLen));
                break;
            }
        }
        return sbPWD.toString();
    }

    public static String randomPWD(char[] charr, int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < len; ++x) {
            sb.append(charr[r.nextInt(charr.length)]);
        }
        return sb.toString();
    }

    public static String getMySQLPassword(String plainText) {
        try{
            byte[] utf8 = plainText.getBytes("UTF-8");
            byte[] test = DigestUtils.sha(DigestUtils.sha(utf8));
            return "*" + convertToHex(test).toUpperCase();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
}
