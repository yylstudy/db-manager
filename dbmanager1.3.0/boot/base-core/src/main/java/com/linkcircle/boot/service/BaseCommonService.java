package com.linkcircle.boot.service;

import com.linkcircle.boot.common.api.dto.LogDTO;
import com.linkcircle.boot.common.system.vo.LoginUser;

/**
 * common接口
 */
public interface BaseCommonService {

    /**
     * 保存日志
     * @param logDTO
     */
    void addLog(LogDTO logDTO);

    /**
     * 保存日志
     * @param LogContent
     * @param logType
     * @param operateType
     * @param user
     */
    void addLog(String LogContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * 保存日志
     * @param LogContent
     * @param logType
     * @param operateType
     */
    void addLog(String LogContent, Integer logType, Integer operateType);

}
