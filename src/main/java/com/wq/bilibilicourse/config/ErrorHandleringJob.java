package com.wq.bilibilicourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 该任务包含两个Step - Step1 Step2，Step中运行相同的Tasklet。
 * 首先执行Step1，Step1在第1次运行的时候 主动抛出异常；然后重启任务，
 * 会从Step1重新开始执行，第2次运行返回RepeatStatus.FINISHED可以开始执行Step2
 * Step2在第1次运行的时候 主动抛出异常；然后重启任务，
 * 会从Step2重新开始执行，第2次运行返回RepeatStatus.FINISHED，任务结束
 */
@Configuration
@EnableBatchProcessing
public class ErrorHandleringJob {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job myErrorHandlingJob(){
        return jobBuilderFactory.get("myErrorHandlingJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.errorStep1())
                .next(this.errorStep2())
                .build();

    }
    @Bean
    public Step errorStep1(){
        return stepBuilderFactory.get("errorStep1")
                .tasklet(this.errorHandling())
                .build();
    }
    @Bean
    public Step errorStep2(){
        return stepBuilderFactory.get("errorStep2")
                .tasklet(this.errorHandling())
                .build();
    }
    @Bean
    @StepScope
    public Tasklet errorHandling(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                // 获取Step的 执行上下文stepExecutionContext()
                Map<String, Object> stepExecution = chunkContext.getStepContext().getStepExecutionContext();
                if(stepExecution.containsKey("wqiang")){
                    System.out.println("This step run twice and success now!");
                    // 只有当前Step返回RepeatStatus.FINISHED;才会执行下1个Step
                    // 否则 如果当前Step执行失败，重启任务，还是会再次重新执行上次失败的Step
                    return RepeatStatus.FINISHED;
                }else{
                    System.out.println("This step run first time and fail!");
                    chunkContext.getStepContext().getStepExecution().getExecutionContext().put("wqiang", true);
                    throw new RuntimeException("第一次执行抛出异常");
                }
            }
        };
    }
}
