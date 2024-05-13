package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.entity.ComputerRoom;
import com.linkcircle.dbmanager.mapper.ComputerRoomMapper;
import com.linkcircle.dbmanager.service.ComputerRoomService;
import com.linkcircle.dbmanager.util.PwdUtil;
import com.linkcircle.dbmanager.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
@Slf4j
public class ComputerRoomServiceImpl extends ServiceImpl<ComputerRoomMapper, ComputerRoom> implements ComputerRoomService {
    @Override
    public void add(ComputerRoom computerRoom) {
        if("1".equals(computerRoom.getEnableRemoteStore())){
            String sshPassword = computerRoom.getSshPassword();
            String pwd = PwdUtil.encryptPwd(sshPassword);
            checkSftpAndbasePath(computerRoom,sshPassword);
            computerRoom.setSshPassword(pwd);
        }
        this.save(computerRoom);
    }

    @Override
    public void edit(ComputerRoom computerRoom) {
        if("1".equals(computerRoom.getEnableRemoteStore())){
            String sftpPassword = computerRoom.getSshPassword();
            if(StringUtils.hasText(sftpPassword)){
                computerRoom.setSshPassword(PwdUtil.encryptPwd(sftpPassword));
            }else{
                ComputerRoom room = this.getById(computerRoom.getId());
                sftpPassword = PwdUtil.decryptPwd(room.getSshPassword());
            }
            checkSftpAndbasePath(computerRoom,sftpPassword);
        }
        this.updateById(computerRoom);
    }

    private void checkSftpAndbasePath(ComputerRoom computerRoom,String sftpPassword){
        Session session = null;
        try{
            session = SshUtil.getSession(computerRoom.getSshIp(),computerRoom.getSshPort()
                    ,computerRoom.getSshUsername(),sftpPassword);
            Integer execStatus = SshUtil.getExecStatus(session,"cd "+computerRoom.getBasePath());
            if(execStatus!=0){
                throw new BusinessException("目录不存在");
            }
        }finally {
            if (session !=null){
                session.disconnect();
            }
        }
    }
}
