package com.lonkcircle.dbmanager;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import org.apache.commons.io.IOUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/8/22 11:45
 */

public class MyTest2 {
    public static void main(String[] args)
            throws Exception {

        ApiClient client = Config.fromConfig("D:\\config-prod");
        CoreV1Api api = new CoreV1Api(client);
        Exec exec = new Exec(client);
        V1Pod v1Pod = api.readNamespacedPod("mysql-cluster-1", "water",null);
        long t1 = System.currentTimeMillis();
        System.out.println(t1);
        Process process =
                    exec.exec(v1Pod, new String[]{"sh","-c","rm -rf /home/dbmanager/backupfiles/backupLog && xtrabackup --backup --compress " +
                                    " --compress-threads=4  --host=10.100.243.23 --port=31510 --user=root --password=\"cqt@mysql1234\" --no-timestamp " +
                                    "  --target-dir=/home/dbmanager/backupfiles/full20230905173731 > /home/dbmanager/backupfiles/backupLog 2>&1 &&" +
                                    " cat /home/dbmanager/backupfiles/backupLog"},
                        "xtrabackup",
                        true,false);
        System.out.println("--------------");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(()->{
            try {
                String str = IOUtils.toString(process.getInputStream(),"UTF-8");
                return str;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<String> errorCompletableFuture = CompletableFuture.supplyAsync(()->{
            try{
                return IOUtils.toString(process.getErrorStream(),"UTF-8");
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }) ;
        process.waitFor();
        System.out.println(System.currentTimeMillis()-t1);
        System.out.println();
        String errorStr = errorCompletableFuture.get();
        System.out.println("errorStr:"+errorStr);
        String str = completableFuture.get();
        System.out.println("str:"+str);
//        Thread.sleep(1000);
//        System.out.println("str:"+str);
//        process.destroy();
        int exitValue = process.exitValue();
        System.out.println(exitValue);
//        System.out.println("------------------------");
    }

}
