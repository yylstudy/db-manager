package com.linkcircle.dbmanager.common;

import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/8/24 13:46
 */
@Data
public class EncryptDecryptDto {
    private String pwd;
    private String publicKey;
    /**
     * 1加密、2解密
     */
    private String type;

}
