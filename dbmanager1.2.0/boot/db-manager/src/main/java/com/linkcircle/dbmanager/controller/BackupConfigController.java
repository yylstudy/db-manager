package com.linkcircle.dbmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.common.TaskTypeEnum;
import com.linkcircle.dbmanager.entity.BackupConfig;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.service.BackupConfigService;
import com.linkcircle.dbmanager.service.JobInfoService;
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
@RequestMapping("/backup")
public class BackupConfigController {
    @Autowired
    private BackupConfigService backupConfigService;
    @Autowired
    private JobInfoService jobInfoService;


    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<BackupConfig>> queryPageList(BackupConfig backupConfig, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<BackupConfig> page = new Page<BackupConfig>(pageNo, pageSize);
        IPage<BackupConfig> pageList = backupConfigService.queryPage(page, backupConfig);
        return Result.OK(pageList);
    }
    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<BackupConfig> add(@RequestBody BackupConfig backupConfig) {
        backupConfig.setStatus(0);
        backupConfigService.save(backupConfig);
        return Result.OK();
    }

    /**
     * @param
     * @return
     */
    @RequestMapping(value = "checkSoftInstall", method = RequestMethod.POST)
    public Result checkSoftInstall(@RequestParam Long id) {
        return backupConfigService.active(id);
    }
    /**
     * @param
     * @return
     */
    @RequestMapping(value = "installSoftware", method = RequestMethod.POST)
    public Result installSoftware(@RequestParam Long id,@RequestParam String type) {
        return backupConfigService.installSoftware(id,type);
    }
    @RequestMapping(value = "testSsh", method = RequestMethod.POST)
    public Result testSsh(@RequestBody BackupConfig backupConfig) {
        return backupConfigService.testSsh(backupConfig);

    }
    @RequestMapping(value = "testmysql", method = RequestMethod.POST)
    public Result testmysql(@RequestBody BackupConfig backupConfig) {
        return backupConfigService.testmysql(backupConfig);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "recoverPrepare", method = RequestMethod.POST)
    public Result recoverPrepare(@RequestBody BackupConfig backupConfig) {
        return backupConfigService.recoverPrepare(backupConfig);
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<BackupConfig> edit(@RequestBody BackupConfig backupConfig) {
        backupConfig.setStatus(0);
        backupConfigService.updateById(backupConfig);
        return Result.OK();
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        LambdaQueryWrapper<JobInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(JobInfo::getConfigId,id);
        wrapper.eq(JobInfo::getTaskType, TaskTypeEnum.BACK_MYSQL.getCode());
        int count = jobInfoService.count(wrapper);
        if(count>0){
            return Result.error("删除失败，存在关联此配置的定时任务配置");
        }
        backupConfigService.removeById(id);
        return Result.OK();
    }

}
