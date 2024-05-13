package com.linkcircle.dbmanager.thread;

import com.alibaba.fastjson.JSONObject;
import com.linkcircle.boot.service.ApplicationContextSupport;
import com.linkcircle.dbmanager.cron.CronExpression;
import com.linkcircle.dbmanager.entity.JobInfo;
import com.linkcircle.dbmanager.mapper.JobInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-05-21
 */
@Slf4j
public class JobScheduleHelper {

    private static JobScheduleHelper instance = new JobScheduleHelper();
    public static JobScheduleHelper getInstance(){
        return instance;
    }



    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private volatile static Map<Integer, List<Long>> ringData = new ConcurrentHashMap<>();
    /**
     * 分布式锁数据库配置
     */
    private volatile static Prop prop;
    private int executeTime = 60000;
    private int hasTaskTime = 60000;
    /**
     * pre read
     */
    public static long PRE_READ_MS = 60000;

    public void start(){
        Environment environment = ApplicationContextSupport.getEnvironment();
        String executeTimeStr = environment.getProperty("executeTime");
        String hasTaskTimeStr = environment.getProperty("hasTaskTime");
        String noTaskTime = environment.getProperty("noTaskTime");
        if(!StringUtils.isEmpty(executeTimeStr)){
            executeTime = Integer.parseInt(executeTimeStr);
        }
        if(!StringUtils.isEmpty(hasTaskTimeStr)){
            hasTaskTime = Integer.parseInt(hasTaskTimeStr);
        }
        if(!StringUtils.isEmpty(noTaskTime)){
            JobScheduleHelper.PRE_READ_MS = Integer.parseInt(noTaskTime);
        }
        log.info("executeTime:{}",executeTime);
        log.info("hasTaskTime:{}",hasTaskTime);
        log.info("PRE_READ_MS:{}",PRE_READ_MS);
        scheduleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis()%1000 );
                } catch (InterruptedException e) {
                    if (!scheduleThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
                log.info(">>>>>>>>> init xxl-job admin scheduler success.");
                int preReadCount = 10;
                while (!scheduleThreadToStop) {
                    // Scan Job
                    long start = System.currentTimeMillis();
                    Connection conn = null;
                    Boolean connAutoCommit = null;
                    PreparedStatement preparedStatement = null;
                    PreparedStatement updatePreparedStatement = null;
                    boolean preReadSuc = true;
                    try {
                        conn = getConnection();
                        connAutoCommit = conn.getAutoCommit();
                        conn.setAutoCommit(false);
                        preparedStatement = conn.prepareStatement(  "select * from job_lock where lock_name = 'schedule_lock' for update" );
                        preparedStatement.execute();
                        long nowTime = System.currentTimeMillis();
                        List<JobInfo> scheduleList = scheduleJobQuery(nowTime + PRE_READ_MS, preReadCount,conn);
                        log.info("scheduleList:{}", JSONObject.toJSONString(scheduleList));
                        if (scheduleList!=null && scheduleList.size()>0) {
                            // 2、push time-ring
                            for (JobInfo jobInfo: scheduleList) {
                                // time-ring jump
                                if (nowTime > jobInfo.getTriggerNextTime() + PRE_READ_MS) {
                                    // 2.1、trigger-expire > 5s：pass && make next-trigger-time
                                    log.warn(">>>>>>>>>>> xxl-job, schedule misfire, jobId = " + jobInfo.getId());
                                    refreshNextValidTime(jobInfo, new Date());
                                } else if (nowTime > jobInfo.getTriggerNextTime()) {
                                    // 2.2、trigger-expire < 5s：direct-trigger && make next-trigger-time
                                    // 1、trigger
                                    JobTriggerService jobTriggerService = ApplicationContextSupport.getBean(JobTriggerService.class);
                                    jobTriggerService.addTrigger(jobInfo,true);
                                    log.debug(">>>>>>>>>>> xxl-job, schedule push trigger : jobId = " + jobInfo.getId() );
                                    // 2、fresh next
                                    refreshNextValidTime(jobInfo, new Date());
                                    // next-trigger-time in 5s, pre-read again
                                    if (jobInfo.getTriggerStatus()==1 && nowTime + PRE_READ_MS > jobInfo.getTriggerNextTime()) {
                                        // 1、make ring second
                                        int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);
                                        // 2、push time ring
                                        pushTimeRing(ringSecond, jobInfo.getId());
                                        // 3、fresh next
                                        refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));
                                    }

                                } else {
                                    // 2.3、trigger-pre-read：time-ring trigger && make next-trigger-time
                                    // 1、make ring second
                                    int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);
                                    // 2、push time ring
                                    pushTimeRing(ringSecond, jobInfo.getId());
                                    // 3、fresh next
                                    refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));
                                }
                            }
                            String updateSql = "UPDATE job_info " +
                                    "SET " +
                                    "trigger_last_time = ?, " +
                                    "trigger_next_time = ?, " +
                                    "trigger_status = ? " +
                                    "WHERE id = ?";
                            updatePreparedStatement = conn.prepareStatement(updateSql );
                            // 3、update trigger info
                            for (JobInfo jobInfo: scheduleList) {
                                updatePreparedStatement.setLong(1,jobInfo.getTriggerLastTime());
                                updatePreparedStatement.setLong(2,jobInfo.getTriggerNextTime());
                                updatePreparedStatement.setInt(3,jobInfo.getTriggerStatus());
                                updatePreparedStatement.setLong(4,jobInfo.getId());
                                updatePreparedStatement.addBatch();
                            }
                            updatePreparedStatement.executeBatch();

                        } else {
                            preReadSuc = false;
                        }
                    } catch (Exception e) {
                        if (!scheduleThreadToStop) {
                            log.error(">>>>>>>>>>> xxl-job, JobScheduleHelper#scheduleThread error:{}", e);
                        }
                    } finally {
                        // commit
                        if (conn != null) {
                            try {
                                conn.commit();
                            } catch (SQLException e) {
                                if (!scheduleThreadToStop) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                            try {
                                conn.setAutoCommit(connAutoCommit);
                            } catch (SQLException e) {
                                if (!scheduleThreadToStop) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                if (!scheduleThreadToStop) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                        // close PreparedStatement
                        if (null != updatePreparedStatement) {
                            try {
                                updatePreparedStatement.close();
                            } catch (SQLException e) {
                                if (!scheduleThreadToStop) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                        // close PreparedStatement
                        if (null != preparedStatement) {
                            try {
                                preparedStatement.close();
                            } catch (SQLException e) {
                                if (!scheduleThreadToStop) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }

                    }
                    long cost = System.currentTimeMillis()-start;


                    // Wait seconds, align second
                    if (cost < executeTime) {  // scan-overtime, not wait
                        try {
                            // pre-read period: success > scan each second; fail > skip this period;
                            TimeUnit.MILLISECONDS.sleep((preReadSuc?hasTaskTime:PRE_READ_MS) - System.currentTimeMillis()%1000);
                        } catch (InterruptedException e) {
                            if (!scheduleThreadToStop) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }

                }

                log.info(">>>>>>>>>>> xxl-job, JobScheduleHelper#scheduleThread stop");
            }
        });
        scheduleThread.setDaemon(true);
        scheduleThread.setName("xxl-job, admin JobScheduleHelper#scheduleThread");
        scheduleThread.start();


        // ring thread
        ringThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // align second
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis()%1000 );
                } catch (InterruptedException e) {
                    if (!ringThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }

                while (!ringThreadToStop) {

                    try {
                        // second data
                        List<Long> ringItemData = new ArrayList<>();
                        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                        for (int i = 0; i < 2; i++) {
                            List<Long> tmpData = ringData.remove( (nowSecond+60-i)%60 );
                            if (tmpData != null) {
                                ringItemData.addAll(tmpData);
                            }
                        }

                        // ring trigger
                        log.debug(">>>>>>>>>>> xxl-job, time-ring beat : " + nowSecond + " = " + Arrays.asList(ringItemData) );
                        if (ringItemData.size() > 0) {
                            JobTriggerService jobTriggerService = ApplicationContextSupport.getBean(JobTriggerService.class);
                            // do trigger
                            for (Long jobId: ringItemData) {
                                JobInfoMapper jobInfoMapper = ApplicationContextSupport.getBean(JobInfoMapper.class);
                                JobInfo jobInfo = jobInfoMapper.selectById(jobId);
                                // do trigger
                                jobTriggerService.addTrigger(jobInfo,true);
                            }
                            // clear
                            ringItemData.clear();
                        }
                    } catch (Exception e) {
                        if (!ringThreadToStop) {
                            log.error(">>>>>>>>>>> xxl-job, JobScheduleHelper#ringThread error:{}", e);
                        }
                    }

                    // next second, align second
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis()%1000);
                    } catch (InterruptedException e) {
                        if (!ringThreadToStop) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
                log.info(">>>>>>>>>>> xxl-job, JobScheduleHelper#ringThread stop");
            }
        });
        ringThread.setDaemon(true);
        ringThread.setName("xxl-job, admin JobScheduleHelper#ringThread");
        ringThread.start();
    }

    private List<JobInfo> scheduleJobQuery( long maxNextTime, int pagesize,Connection conn ){
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT t.id,\n" +
                "                t.job_cron,\n" +
                "                t.create_time,\n" +
                "                t.trigger_last_time,\n" +
                "                t.trigger_next_time,\n" +
                "                t.trigger_status,\n" +
                "                t.param,\n" +
                "                t.config_id,\n" +
                "                t.task_type\n" +
                "                FROM job_info AS t\n" +
                "        WHERE t.trigger_status = 1\n" +
                "        and t.trigger_next_time <=" +maxNextTime+
                "        ORDER BY id ASC\n" +
                "        LIMIT "+pagesize;
        try{
            ps = conn.prepareStatement(  sql );
            rs = ps.executeQuery();
            List<JobInfo> list = new ArrayList<>();
            while (rs.next()){
                JobInfo jobInfo = new JobInfo();
                jobInfo.setId(rs.getLong(1));
                jobInfo.setJobCron(rs.getString(2));
                jobInfo.setCreateTime(rs.getDate(3));
                jobInfo.setTriggerLastTime(rs.getLong(4));
                jobInfo.setTriggerNextTime(rs.getLong(5));
                jobInfo.setTriggerStatus(rs.getInt(6));
                jobInfo.setParam(rs.getString(7));
                jobInfo.setConfigId(rs.getLong(8));
                jobInfo.setTaskType(rs.getString(9));
                list.add(jobInfo);
            }
            return list;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            if (null != ps) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    if (!scheduleThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    if (!scheduleThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

    }


    private  Connection getConnection(){
        if(prop==null){
            synchronized (JobScheduleHelper.class){
                if(prop==null){
                    Environment environment = ApplicationContextSupport.getEnvironment();
                    String url = environment.getProperty("spring.datasource.druid.url");
                    String user = environment.getProperty("spring.datasource.druid.username");
                    String password = environment.getProperty("spring.datasource.druid.password");
                    String driverClass = environment.getProperty("spring.datasource.druid.driver-class-name");
                    prop = new Prop(url,user,password,driverClass);
                }
            }
        }
        try {
            Connection conn = DriverManager.getConnection(prop.url, prop.user, prop.password);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshNextValidTime(JobInfo jobInfo, Date fromTime) throws ParseException {
        Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
        } else {
            jobInfo.setTriggerStatus(0);
            jobInfo.setTriggerLastTime(0L);
            jobInfo.setTriggerNextTime(0L);
        }
    }

    private void pushTimeRing(int ringSecond, Long jobId){
        // push async ring
        List<Long> ringItemData = ringData.get(ringSecond);
        if (ringItemData == null) {
            ringItemData = new ArrayList<Long>();
            ringData.put(ringSecond, ringItemData);
        }
        ringItemData.add(jobId);

        log.debug(">>>>>>>>>>> xxl-job, schedule push time-ring : " + ringSecond + " = " + Arrays.asList(ringItemData) );
    }


    static class Prop{
        private  String url ;
        private  String user;
        private  String password;
        private  String driverClass;

        public Prop(String url, String user, String password, String driverClass) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.driverClass = driverClass;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
        }
    }

}
