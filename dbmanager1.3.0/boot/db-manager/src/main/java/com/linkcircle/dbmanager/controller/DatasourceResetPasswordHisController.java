package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.entity.DatasourceResetPasswordHis;
import com.linkcircle.dbmanager.service.DatasourceResetPasswordHisService;
import lombok.extern.slf4j.Slf4j;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    @Value("${common.path.upload}")
    private String upLoadPath;
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

    @GetMapping(value = "/exportExcel")
    public ModelAndView exportExcel(DatasourceResetPasswordHis his) {
        Page<DatasourceResetPasswordHis> page = new Page<>(1, 1000000);
        List<DatasourceResetPasswordHis> passwordHisList = hisService.queryPage(page, his).getRecords();
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "密码修改历史报表");
        mv.addObject(NormalExcelConstants.CLASS, DatasourceResetPasswordHis.class);
        ExportParams exportParams=new ExportParams(null, null, "密码修改历史");
        exportParams.setImageBasePath(upLoadPath);
        mv.addObject(NormalExcelConstants.PARAMS,exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, passwordHisList);
        return mv;
    }
}
