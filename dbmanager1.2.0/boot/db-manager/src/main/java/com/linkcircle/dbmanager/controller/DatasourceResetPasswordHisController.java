package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.DatasourceResetPasswordHis;
import com.linkcircle.dbmanager.service.DatasourceResetPasswordHisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/5/11 10:40
 */
@RestController
@Slf4j
@RequestMapping("/passwordHis")
public class DatasourceResetPasswordHisController {
    @Autowired
    private DatasourceResetPasswordHisService hisService;

    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<IPage<DatasourceResetPasswordHis>> queryPageList(DatasourceResetPasswordHis his, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                     @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<DatasourceResetPasswordHis> page = new Page<DatasourceResetPasswordHis>(pageNo, pageSize);
        IPage<DatasourceResetPasswordHis> pageList = hisService.queryPage(page, his);
        return Result.OK(pageList);
    }
}
