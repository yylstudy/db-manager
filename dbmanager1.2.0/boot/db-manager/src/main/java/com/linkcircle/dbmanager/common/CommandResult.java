package com.linkcircle.dbmanager.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/9/17 11:14
 */
@Data
@AllArgsConstructor
public class CommandResult {
    private boolean success;
    private String msg;
}
