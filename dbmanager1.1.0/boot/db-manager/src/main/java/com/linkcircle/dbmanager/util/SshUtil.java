package com.linkcircle.dbmanager.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.common.ExecResult;
import com.linkcircle.dbmanager.common.LinkcircleFunction;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-16 11:47
 **/
@Slf4j
public class SshUtil {


    /**
     * 获取会话
     * @param ip
     * @param port
     * @param user
     * @param password
     * @return
     */
    public static Session getSession(String ip, int port, String user, String password) {
        try{
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(user, ip, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            return sshSession;
        }catch (Exception e){
            throw new BusinessException(ip+" ssh配置错误，获取ssh连接失败");
        }
    }

    public static Integer getExecStatus(Session session,String command) {
        return getExecResult(session,command).getCode();
    }
    public static ExecResult getExecResult(Session session,String command) {
        ChannelExec channelExec = null;
        try{
            channelExec = (ChannelExec)session.openChannel( "exec" );
            channelExec.setCommand(command);
            InputStream in = channelExec.getInputStream();
            InputStream errStream = channelExec.getErrStream();
            channelExec.connect();
            ExecResult execResult = new ExecResult();
            execResult.setMessage(IOUtils.toString(in,"utf-8"));
            execResult.setErrorMessage(IOUtils.toString(errStream,"utf-8"));
            execResult.setCode(channelExec.getExitStatus());
            return execResult;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            if(channelExec!=null){
                channelExec.disconnect();
            }
        }
    }

    public static <T> T doExecute(Session session,LinkcircleFunction<ChannelSftp,T> consumer) throws Exception{
        ChannelSftp channel = null;
        try{
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect(2000);
            T t = consumer.apply(channel);
            return t;
        }finally {
            if(channel!=null){
                channel.disconnect();
            }
        }
    }

    public static void scpDownload(String ip, int port, String user, String password,
                                   String scpSourceDir,String  scpTargetdIR) throws Exception{
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(ip, port);
        ssh.authPassword(user,password);
        try {
            ssh.newSCPFileTransfer().download(scpSourceDir, new FileSystemFile(scpTargetdIR));
        } finally {
            ssh.disconnect();
        }
    }


    public static void deleteFile(ChannelSftp channelSftp,String fileName){
        try{
            channelSftp.rm(fileName);
        }catch (Exception e){
            String message = e.getMessage();
            if(StringUtils.isNotBlank(message)&&message.contains("No such file")){
                log.info("fileName:{} not exists",fileName);
            }else{
                log.error("delete file failure",e);
                throw new RuntimeException("delete file "+fileName+" failure");
            }
        }
    }
    public static String getFileStr(ChannelSftp channelSftp,String fileName){
        String backlog = "";
        try{
            InputStream backlogIs = channelSftp.get(fileName);
            backlog = IOUtils.toString(backlogIs,"utf-8");
            return backlog;
        }catch (Exception e){
            String message = e.getMessage();
            if(StringUtils.isNotBlank(message)&&message.contains("No such file")){
                log.info("fileName:{} not exists",fileName);
                return backlog;
            }else{
                throw new RuntimeException("get file str "+fileName+" error");
            }
        }
    }
}
