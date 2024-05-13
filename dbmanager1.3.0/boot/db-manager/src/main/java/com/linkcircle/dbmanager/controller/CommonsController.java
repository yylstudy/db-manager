package com.linkcircle.dbmanager.controller;

import com.linkcircle.boot.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/10/26 14:53
 */
@RestController
@RequestMapping("/common")
public class CommonsController {
    @Value("${yearningUrl}")
    private String yearningUrl;
    @GetMapping("getYearningUrl")
    public Result<String> getYearningUrl(){
        return Result.OK(yearningUrl);
    }
}
