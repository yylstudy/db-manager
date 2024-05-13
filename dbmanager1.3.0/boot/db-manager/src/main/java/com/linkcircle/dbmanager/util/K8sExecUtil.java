package com.linkcircle.dbmanager.util;

import com.linkcircle.boot.common.exception.BusinessException;
import com.linkcircle.dbmanager.common.CommonConstant;
import com.linkcircle.dbmanager.common.ExecResult;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/23 15:16
 */
@Slf4j
public class K8sExecUtil {

    public static boolean existsDir(Exec exec, V1Pod v1Pod, String path) throws Exception{
        ExecResult execResult = doExec(exec,v1Pod,new String[]{"sh","-c","cd "+path+""});
        Integer code = execResult.getCode();
        return CommonConstant.EXEC_SUCCESS.equals(code);
    }
    public static boolean existsDir(Exec exec, String namespace, String podName, String path) throws Exception{
        ExecResult execResult = doExec(exec,namespace,podName,new String[]{"sh","-c","cd "+path+""});
        Integer code = execResult.getCode();
        return CommonConstant.EXEC_SUCCESS.equals(code);
    }

    public static boolean rmDir(Exec exec, V1Pod v1Pod, String path) throws Exception{
        ExecResult execResult = doExec(exec,v1Pod,new String[]{"sh","-c","rm -rf "+path+""});
        Integer code = execResult.getCode();
        return CommonConstant.EXEC_SUCCESS.equals(code);
    }
    public static boolean rmDir(Exec exec, String namespace,String podName, String path) throws Exception{
        ExecResult execResult = doExec(exec,namespace,podName,new String[]{"sh","-c","rm -rf "+path+""});
        Integer code = execResult.getCode();
        return CommonConstant.EXEC_SUCCESS.equals(code);
    }

    public static List<String> ls(Exec exec, V1Pod v1Pod, String path) throws Exception{
        ExecResult execResult = doExec(exec,v1Pod,new String[]{"sh","-c","cd "+path+" && ls"});
        String dirs = execResult.getMessage();
        Integer code = execResult.getCode();
        if(!CommonConstant.EXEC_SUCCESS.equals(code)){
            throw new BusinessException("获取目录异常,"+dirs);
        }
        return Arrays.asList(dirs.split("\\n"));
    }


    public static ExecResult doExec(Exec exec, V1Pod v1Pod,String[] cmd) throws Exception{
        Process process = null;
        try {
            process = exec.exec(v1Pod,cmd, CommonConstant.XTRABACKUP_DEFAULT_CONTAINER,true,false);
            ExecResult execResult = processToExecResult(process);
            return execResult;
        }finally {
            if(process!=null){
                process.destroy();
            }
        }
    }

    public static ExecResult doExec(Exec exec,String namespace, String podName,String[] cmd) throws Exception{
        Process process = null;
        try {
            process = exec.exec(namespace,podName,cmd, CommonConstant.XTRABACKUP_DEFAULT_CONTAINER,true,false);
            ExecResult execResult = processToExecResult(process);
            return execResult;
        }finally {
            if(process!=null){
                process.destroy();
            }
        }
    }

    /**
     * 有时会报如下异常，不影响业务，官方好像并没有给出解决方案，https://github.com/kubernetes-client/java/issues/1891
     * java.net.SocketException: Connection or outbound has been closed
     * 	at sun.security.ssl.SSLSocketOutputRecord.deliver(SSLSocketOutputRecord.java:289)
     * 	at sun.security.ssl.SSLSocketImpl$AppOutputStream.write(SSLSocketImpl.java:1106)
     * 	at okio.OutputStreamSink.write(Okio.kt:56)
     * 	at okio.AsyncTimeout$sink$1.write(AsyncTimeout.kt:103)
     * 	at okio.RealBufferedSink.flush(RealBufferedSink.kt:247)
     * 	at okhttp3.internal.ws.WebSocketWriter.writeControlFrame(WebSocketWriter.kt:140)
     * 	at okhttp3.internal.ws.WebSocketWriter.writeClose(WebSocketWriter.kt:100)
     * 	at okhttp3.internal.ws.RealWebSocket.writeOneFrame$okhttp(RealWebSocket.kt:470)
     * 	at okhttp3.internal.ws.RealWebSocket$WriterTask.runOnce(RealWebSocket.kt:547)
     * 	at okhttp3.internal.concurrent.TaskRunner.runTask(TaskRunner.kt:116)
     * 	at okhttp3.internal.concurrent.TaskRunner.access$runTask(TaskRunner.kt:42)
     * 	at okhttp3.internal.concurrent.TaskRunner$runnable$1.run(TaskRunner.kt:65)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     * @param process
     * @return
     * @throws Exception
     */
    public static ExecResult processToExecResult(Process process) throws Exception{
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(()->{
            try{
                return IOUtils.toString(process.getInputStream(),"UTF-8");
            }catch (Exception e){
                throw new RuntimeException();
            }
        }) ;
        CompletableFuture<String> errorCompletableFuture = CompletableFuture.supplyAsync(()->{
            try{
                return IOUtils.toString(process.getErrorStream(),"UTF-8");
            }catch (Exception e){
                return "";
            }
        }) ;
        String errorStr = errorCompletableFuture.get();
        process.waitFor();
        String str = completableFuture.get();
        int exitValue = process.exitValue();
        ExecResult execResult = new ExecResult(exitValue,str,errorStr);
        return execResult;
    }

}
