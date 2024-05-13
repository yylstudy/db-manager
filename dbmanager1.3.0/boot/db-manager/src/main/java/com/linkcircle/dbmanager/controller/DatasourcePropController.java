package com.linkcircle.dbmanager.controller;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.boot.common.api.vo.Result;
import com.linkcircle.dbmanager.common.*;
import com.linkcircle.dbmanager.entity.*;
import com.linkcircle.dbmanager.service.*;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@RestController
@Slf4j
@RequestMapping("/datasource")
public class DatasourcePropController {
    @Autowired
    private DatasourcePropService datasourcePropService;
    @Autowired
    private MysqlUserService mysqlUserService;
    @Autowired
    private BackupConfigService backupConfigService;
    @Autowired
    private ClearDataConfigService clearDataConfigService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private DatasourceGroupUserService datasourceGroupUserService;
    /**
     * 获取列表数据
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<?> queryPageList(DatasourceProp datasourceProp, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Page<DatasourceProp> page = new Page<DatasourceProp>(pageNo, pageSize);
        IPage<DatasourceProp> pageList = datasourcePropService.queryPage(page, datasourceProp);
        return Result.OK(pageList);
    }

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestBody DatasourceProp datasourceProp) {
        return datasourcePropService.add(datasourceProp);
    }


    @RequestMapping(value = "/getNamespace")
    public Result<?> getNamespace(@RequestParam  Long k8sConfigId) {
        return datasourcePropService.getNamespace(k8sConfigId);
    }
    @RequestMapping(value = "/getPod")
    public Result<?> getPod(@RequestParam Long k8sConfigId,@RequestParam String namespace) {
        return datasourcePropService.getPod(k8sConfigId,namespace);
    }

    /**
     *   获取真实数据源
     * @param
     * @return
     */
    @GetMapping(value = "/getRealDatabase")
    public Result<?> getRealDatabase(@RequestParam Long mysqlUserId) {
        return datasourcePropService.getRealDatabase(mysqlUserId);
    }
    /**
     *   获取真实数据源
     * @param
     * @return
     */
    @GetMapping(value = "/getRealDatabaseByPropId")
    public Result<?> getRealDatabaseByPropId(@RequestParam Long propId) {
        return datasourcePropService.getRealDatabaseByPropId(propId);
    }

    /**
     *   查询用户列表
     * @param
     * @return
     */
    @RequestMapping(value = "/userList")
    public Result<?> userList(@RequestParam(required = false) Long id) {
        if(id==null){
            return Result.OK(new ArrayList<>());
        }
        return datasourcePropService.userList(id);
    }

    /**
     *   重置组密码
     * @param
     * @return
     */
    @RequestMapping(value = "/resetGroupPwd")
    public Result resetGroupPwd(@RequestBody GroupUserDto groupUserDto) {
        return datasourcePropService.resetGroupPwd(groupUserDto);
    }

    /**
     *   组用户列表
     * @param
     * @return
     */
    @RequestMapping(value = "/userGroupList")
    public Result userGroupList(@RequestParam(required = false) Long groupId) {
        if(groupId==null){
            return Result.OK(new ArrayList<>());
        }
        return datasourcePropService.userGroupList(groupId);
    }
    /**
     *   发送密码
     * @param
     * @return
     */
    @RequestMapping(value = "/sendPassword")
    public Result<?> sendPassword() {
        List<SysUser> sysUserList = sysUserService.queryUserByRoleCode(CommonConstant.PWD_CHANGE_ROLE);
        String emails = sysUserList.stream().map(SysUser::getEmail).collect(Collectors.joining(","));
        datasourcePropService.sendAllPasswordEmail(emails);
        return Result.OK();
    }

    /**
     *   分配组权限
     * @param
     * @return
     */
    @RequestMapping(value = "/grantGroupPrivileges")
    public Result<?> grantGroupPrivileges(@RequestBody GroupUserDto groupUserDto) {
        return datasourcePropService.grantGroupPrivileges(groupUserDto);
    }
    /**
     *   获取用户
     * @param
     * @return
     */
    @RequestMapping(value = "/getMysqlUser")
    public Result<?> getMysqlUser(@RequestBody MysqlUser mysqlUser) {
        MysqlUser dbUser = mysqlUserService.getMysqlUser(mysqlUser.getPropId(),mysqlUser.getUsername());
        if(dbUser==null){
            return Result.OK();
        }
        return Result.OK(dbUser);
    }
    /**
     *   获取用户层级
     * @param
     * @return
     */
    @RequestMapping(value = "/getMysqlUserByLevel")
    public Result<?> getMysqlUserByLevel() {
        return datasourcePropService.getMysqlUserByLevel();
    }



    /**
     *   获取database
     * @param
     * @return
     */
    @RequestMapping(value = "/getDatabase")
    public Result<?> getDatabase(@RequestBody MysqlUser mysqlUser) {
        return datasourcePropService.getDatabase(mysqlUser);
    }

    /**
     *   获取组用户
     * @param
     * @return
     */
    @RequestMapping(value = "/getGroupUser")
    public Result<?> getGroupUser(@RequestBody(required = false) MysqlUser mysqlUser) {
        if(mysqlUser==null){
            return Result.OK(new ArrayList<>());
        }
        List<DbUser> list = datasourcePropService.getGroupUser(mysqlUser.getGroupId(),mysqlUser.getUsername(),true);
        Set<String> allDb = new HashSet<>();
        Set<String> allHost = new HashSet<>();
        for (DbUser dbUser : list) {
            allDb.add(dbUser.getDb());
            allHost.add(dbUser.getHost());
        }
        DatasourceGroupUser datasourceGroupUser = datasourceGroupUserService.findByGroupIdAndUser(mysqlUser.getGroupId(),mysqlUser.getUsername());
        Map<String,Object> map = new HashMap<>();
        map.put("list",list);
        map.put("allDb",allDb.stream().collect(Collectors.joining(",")));
        map.put("allHost",allHost.stream().collect(Collectors.joining(",")));
        if(datasourceGroupUser!=null){
            map.put("password",datasourceGroupUser.getPassword());
        }
        return Result.OK(map);
    }

    /**
     *   重置密码用户列表
     * @param
     * @return
     */
    @RequestMapping(value = "/resetUserList")
    public Result<?> resetUserList(@RequestBody(required = false) MysqlUser mysqlUser) {
        if(mysqlUser==null){
            return Result.OK(new ArrayList<>());
        }
        List<DbUser> list = datasourcePropService.getGroupUser(mysqlUser.getGroupId(),mysqlUser.getUsername(),true);
        Collection<DbUser> collection = list.stream().collect(Collectors.groupingBy(dbUser->dbUser.getIp(),Collectors
                .collectingAndThen(Collectors.reducing((t1,t2)->{
                    t1.setHost(t1.getHost()+","+t2.getHost());
                    t1.setDb(t1.getDb()+","+t2.getDb());
                    if(!t2.isExistsLocal()){
                        t1.setExistsLocal(t2.isExistsLocal());
                    }
                    if(!t2.isPasswordGroupMatch()){
                        t1.setPasswordGroupMatch(t2.isPasswordGroupMatch());
                    }
                    if(!t2.isPasswordMatch()){
                        t1.setPasswordMatch(t2.isPasswordMatch());
                    }
                    return t1;
                }),optional->{
                    DbUser dbUser = optional.get();
                    dbUser.setDb(Arrays.stream(dbUser.getDb().split(",")).distinct().collect(Collectors.joining(",")));
                    return dbUser;
                }))).values();
        return Result.OK(collection);
    }
    /**
     *   重置组用户密码
     * @param
     * @return
     */
    @RequestMapping(value = "/encryptDecryptPwd")
    public Result<?> encryptDecryptPwd(@RequestBody EncryptDecryptDto encryptDecryptDto) {
        try{
            String newPwd;
            if("1".equals(encryptDecryptDto.getType())){
                newPwd = PwdUtil.encryptPwd(encryptDecryptDto.getPwd());
            }else{
                newPwd = PwdUtil.decryptPwd(encryptDecryptDto.getPublicKey(),encryptDecryptDto.getPwd());
            }
            return Result.OK(newPwd);
        }catch (Exception e){
            return Result.error("解密失败，请检查密码、公钥是否正确");
        }
    }

    /**
     *   重置组用户密码
     * @param
     * @return
     */
    @RequestMapping(value = "/resetGroupUserPassword")
    public Result<?> resetGroupUserPassword(@RequestBody ResetGroupUserDto resetGroupUserDto) {
        return datasourcePropService.resetGroupUserPassword(resetGroupUserDto);
    }

    @GetMapping(value = "/getById")
    public Result<?> getById(@RequestParam Long id) {
        return Result.OK(datasourcePropService.getById(id));
    }

    /**
     *   添加组用户
     * @param
     * @return
     */
    @RequestMapping(value = "/addGroupUser")
    public Result<?> addGroupUser(@RequestBody GroupUserDto groupUserDto) {
        return datasourcePropService.addGroupUser(groupUserDto);
    }
    /**
     *   删除组用户
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteGroupUser")
    public Result<?> deleteGroupUser(@RequestBody GroupUserDto groupUserDto) {
        return datasourcePropService.deleteGroupUser(groupUserDto);
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public Result<?> edit(@RequestBody DatasourceProp datasourceProp) {
        return datasourcePropService.edit(datasourceProp);
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name="id") String id) {
        LambdaQueryWrapper<BackupConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(BackupConfig::getPropId,id);
        long count = backupConfigService.count(wrapper);
        if(count!=0){
            return Result.error("删除失败，存在关联此数据源的备份规则");
        }
        count = clearDataConfigService.countByPropId(Long.parseLong(id));
        if(count!=0){
            return Result.error("删除失败，存在关联此数据源的清理规则");
        }
        DatasourceProp datasourceProp = datasourcePropService.getById(id);
        if(datasourceProp.getGroupId()!=null){
            return Result.error("删除失败，存在关联此数据源的组");
        }
        return datasourcePropService.delete(id);
    }
    @RequestMapping(value = "/decryptPwd")
    public String decryptPwd(@RequestBody JSONObject jsonObject) throws Exception{
        String password = jsonObject.getString("password");
        String decryptPwd = ConfigTools.decrypt("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKV9oPKXNybH9Q6sh9uBk6uvoB70TF0jOVz9K" +
                "ltqCmvD9D11w4ab+mS1oYCrkOURM/VZoxruCpj3vTNYNqOvUTsCAwEAAQ==",password);
        return decryptPwd;
    }

}
