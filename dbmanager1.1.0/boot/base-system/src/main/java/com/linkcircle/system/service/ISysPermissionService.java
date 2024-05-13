package com.linkcircle.system.service;

import java.util.List;

import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.system.entity.SysPermission;
import com.linkcircle.system.model.TreeModel;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysPermissionService extends IService<SysPermission> {
	
	public List<TreeModel> queryListByParentId(String parentId);
	
	/**真实删除*/
	public void deletePermission(String id) throws BusinessException;
	/**逻辑删除*/
	public void deletePermissionLogical(String id) throws BusinessException;
	
	public void addPermission(SysPermission sysPermission) throws BusinessException;
	
	public void editPermission(SysPermission sysPermission) throws BusinessException;
	
	public List<SysPermission> queryByUser(String username);
	
	/**
	  * 查询出带有特殊符号的菜单地址的集合
	 * @return
	 */
	public List<String> queryPermissionUrlWithStar();

	/**
	 * 判断用户否拥有权限
	 * @param username
	 * @param sysPermission
	 * @return
	 */
	public boolean hasPermission(String username, SysPermission sysPermission);

	/**
	 * 根据用户和请求地址判断是否有此权限
	 * @param username
	 * @param url
	 * @return
	 */
	public boolean hasPermission(String username, String url);
}
