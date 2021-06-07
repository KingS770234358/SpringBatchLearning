package com.wq.bilibilicourse.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * 决策器的实现
 */
public class MyDecider implements JobExecutionDecider {
    private int cnt = 0;
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        cnt++;
        if(cnt%2==1)
            return new FlowExecutionStatus("odd");
        else
            return new FlowExecutionStatus("even");
    }
}
