package com.linkcircle.boot.thread;

import com.linkcircle.boot.service.ApplicationContextSupport;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/9 11:15
 */
public class ThreadPoolFactory {

    public static Map<Long,ThreadPoolExecutor> computerRoomThreadPoolMap = new ConcurrentHashMap<>();

    public static ThreadPoolExecutor getFastTriggerPool(Long compoterRoomId) {
        return computerRoomThreadPoolMap.computeIfAbsent(compoterRoomId,key->{
            int threadNum = ApplicationContextSupport.getEnvironment().getProperty("thread.num",Integer.class,2);
            ThreadPoolExecutor fastTriggerPool = new ThreadPoolExecutor(
                    threadNum,
                    threadNum,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, key+"-task-thread" + r.hashCode());
                        }
                    });
            return fastTriggerPool;
        });
    }

    private static ThreadPoolExecutor sysLogPool = new ThreadPoolExecutor(
            1,
            1,
            1,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "syslogPool-" + r.hashCode());
                }
            });


    public static ThreadPoolExecutor getSysLogPool() {
        return sysLogPool;
    }

    public static ThreadPoolExecutor getScpThreadPool() {
        return scpThreadPool;
    }

    private static ThreadPoolExecutor scpThreadPool = new ThreadPoolExecutor(
            10,
            10,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "scp thread-" + r.hashCode());
                }
            });
}
