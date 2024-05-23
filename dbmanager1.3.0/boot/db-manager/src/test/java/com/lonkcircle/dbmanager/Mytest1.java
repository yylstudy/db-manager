package com.lonkcircle.dbmanager;

import com.alibaba.druid.filter.config.ConfigTools;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/12 9:21
 */
public class Mytest1 {
    public static void main(String[] args) throws Exception{
//        Session session = SshUtil.getSession("172.16.252.130",56022,"root","fjcqt@1234");
//        StringBuilder cpCommand = new StringBuilder("cp -rf /home/dist/1.txt /root");
//        ExecResult execResult = SshUtil.getExecResult(session,cpCommand.toString());
//        System.out.println(execResult);
        String pwd = ConfigTools.encrypt("MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEApX2g8pc3Jsf1DqyH24GTq6+gHvRMXSM5XP0qW2oKa8P0PXXDhpv6ZLWhgKuQ5REz9VmjGu4KmPe9M1g2o69ROwIDAQABAkEAlJuKwy2dPfsDiWjPGwNx+xNb41AXnu95nJJOzgYcU4YOQC5J0/a7Gb+VCSDC2YgLReom065o3lYBhEcDFYstCQIhAPHCQwakOURihwKNxjB1bLyDYKt8ftaxN9xXWe/1A0CvAiEArz09P1ALveSXNUfX0587R+VYSyknYwDklGYSb8dFIzUCIBul8tJkn6QBfJ0/J4ZNN5VLlRenkj3tYI0TdFar96ZDAiEAjScBz561ZobbeVJeOZq4EGhfH2ON00Rj2lkBnsHfIcUCIHQkJltftdtFFxm6I0FS2Y0FapfpN7DHXw4zRoL5JFfd","7tZ7gMK8X8uE");
        System.out.println(pwd);
        String ss = ConfigTools.decrypt("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKV9oPKXNybH9Q6sh9uBk6uvoB70TF0jOVz9KltqCmvD9D11w4ab+mS1oYCrkOURM/VZoxruCpj3vTNYNqOvUTsCAwEAAQ==","TeEEB/HB4W2kHkXgdBTaVRc113ypytTUWdJox+OAtMwFHmR5iK6/91ETdhhfUCfOREB7iKIJgSIDACvtTg0o+A==");
        System.out.println(ss);
    }
}
