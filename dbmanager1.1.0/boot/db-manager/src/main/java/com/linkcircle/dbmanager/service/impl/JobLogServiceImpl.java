package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.boot.common.util.CommonUtils;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.mapper.JobLogMapper;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLog> implements JobLogService {
    @Autowired
    private JobInfoService jobInfoService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Override
    public IPage<JobLog> queryPage(Page<JobLog> page, JobLog jobLog) {
        return this.baseMapper.queryPage(page,jobLog);
    }

    @Override
    public IPage<Map<String, String>> getIbdFrm(Page<Map<String, String>> page, Long id) {
        Session session = null;
        try{
            JobLog jobLog = this.getById(id);
            Long jobId = jobLog.getJobId();
            JobInfo jobInfo = jobInfoService.getById(jobId);
            if(jobInfo==null){
                return new Page();
            }
            ClearDataConfig clearDataConfig = clearDataConfigService.getById(jobInfo.getConfigId());
            if(clearDataConfig==null){
                return new Page();
            }
            MysqlUser mysqlUser = mysqlUserService.getFullMysqlUser(clearDataConfig.getMysqlUserId());
            DatasourceProp prop = datasourcePropService.getById(mysqlUser.getPropId());
            int port = prop.getSshPort();
            String sshUser = prop.getSshUser();
            String sshPassword = prop.getSshPassword();
            session = SshUtil.getSession(mysqlUser.getIp(),port, sshUser, PwdUtil.decryptPwd(sshPassword));
            SshUtil.doExecute(session,channelSftp -> {
                List<ChannelSftp.LsEntry> list = channelSftp.ls(jobLog.getIbdfrmPath());
                List<ChannelSftp.LsEntry> fileList = list.stream().filter(entry->{
                    String fileName = entry.getFilename();
                    return fileName.contains(CommonConstant.IBD_SUFFIX)||
                            fileName.contains(CommonConstant.FRM_SUFFIX);
                }).collect(Collectors.toList());
                List<Map<String,String>> targetList = fileList.stream().skip((page.getCurrent()-1)*page.getSize()).limit(page.getSize())
                        .map(entry->{
                            Map<String,String> map = new HashMap<>();
                            long size = entry.getAttrs().getSize();
                            map.put("fileName",entry.getFilename());
                            map.put("fileSize", CommonUtils.getNetFileSizeDescription(size));
                            return map;
                        })
                        .collect(Collectors.toList());
                page.setRecords(targetList);
                page.setTotal(fileList.size());
                return null;
            });
            return page;
        }catch (Exception e){
            log.error("获取ibd/frm失败",e);
            throw new BusinessException("获取ibd/frm失败");
        }finally{
            if(session!=null){
                session.disconnect();
            }
        }
    }
}
