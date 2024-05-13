package com.linkcircle.dbmanager.common;

import lombok.Data;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/9 16:06
 */
@Data
public class ExecResult {
    private Integer code;
    private String message;
    private String errorMessage;
}
