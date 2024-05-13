package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.linkcircle.boot.common.system.vo.LoginUser;
import com.linkcircle.boot.common.util.IPUtils;
import com.linkcircle.boot.common.util.SpringContextUtils;
import com.linkcircle.boot.common.util.oConvertUtils;
import com.linkcircle.boot.thread.ThreadPoolFactory;
import com.linkcircle.system.entity.SysLog;
import com.linkcircle.system.mapper.SysLogMapper;
import com.linkcircle.system.service.ISysBaseAPI;
import com.linkcircle.system.service.ISysLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统日志表 服务实现类
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@Service
@Slf4j
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

	@Resource
	private SysLogMapper sysLogMapper;
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	
	/**
	 * @功能：清空所有日志记录
	 */
	@Override
	public void removeAll() {
		sysLogMapper.removeAll();
	}

	@Override
	public Long findTotalVisitCount() {
		return sysLogMapper.findTotalVisitCount();
	}

	//update-begin--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
	@Override
	public Long findTodayVisitCount(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayVisitCount(dayStart,dayEnd);
	}

	@Override
	public Long findTodayIp(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayIp(dayStart,dayEnd);
	}
	//update-end--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数

	@Override
	public List<Map<String,Object>> findVisitCount(Date dayStart, Date dayEnd) {
		DbType dbType = getDatabaseTypeEnum();
		return sysLogMapper.findVisitCount(dayStart, dayEnd,dbType.getDb());
	}
	private static DbType dbTypeEnum = null;
	public static DbType getDatabaseTypeEnum() {
		if (oConvertUtils.isNotEmpty(dbTypeEnum)) {
			return dbTypeEnum;
		}
		try {
			DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
			dbTypeEnum = JdbcUtils.getDbType(dataSource.getConnection().getMetaData().getURL());
			return dbTypeEnum;
		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
			return null;
		}
	}
	@Async
	@Override
	public void add(String logContent) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
		ThreadPoolFactory.getSysLogPool().execute(()->{
			SysLog sysLog = new SysLog();
			sysLog.setUserid(user.getUsername());
			sysLog.setUsername(user.getRealname());
			sysLog.setIp(IPUtils.getIpAddr(request));
			sysLog.setLogContent(logContent);
			this.save(sysLog);
		});

	}
}
