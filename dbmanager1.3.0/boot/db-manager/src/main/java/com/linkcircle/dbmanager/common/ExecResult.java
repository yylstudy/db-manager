package com.linkcircle.dbmanager.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/9 16:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecResult {
    private Integer code;
    private String message;
    private String errorMessage;
}
