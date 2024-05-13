package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.JobInfoHis;
import com.linkcircle.dbmanager.service.JobInfoHisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/jobinfoHis")
public class JobInfoHisController {
    @Autowired
    private JobInfoHisService jobInfoHisService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<JobInfoHis>> queryPageList(JobInfoHis jobInfoHis, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<JobInfoHis> page = new Page<JobInfoHis>(pageNo, pageSize);
        IPage<JobInfoHis> pageList = jobInfoHisService.queryPage(page, jobInfoHis);
        return Result.OK(pageList);
    }


    @PostMapping("/recovery")
    public Result recovery(Long id) {
        return jobInfoHisService.recovery(id);
    }

}
