package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author zhexueqi
 * @ClassName MyTask
 * @since 2024/2/29    11:16
 */
@Component
@Slf4j
public class MyTask {

    @Scheduled(cron = "0/5 * * * * *")
    public void myTask(){
        log.info("定时任务执行:{}", new Date());
    }
}
