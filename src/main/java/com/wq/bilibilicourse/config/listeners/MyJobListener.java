package com.wq.bilibilicourse.config.listeners;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;


/**
 * JobExecution的监听器
 * 接口方式
 */
public class MyJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 可以通过JobExecution对象获得Job实例，再获得Job的相关信息
        System.out.println(jobExecution.getJobInstance().getJobName() + "before...");

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println(jobExecution.getJobInstance().getJobName() + "after...");
    }
}
