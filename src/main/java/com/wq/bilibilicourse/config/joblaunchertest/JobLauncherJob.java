package com.wq.bilibilicourse.config.joblaunchertest;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableBatchProcessing
public class JobLauncherJob implements StepExecutionListener {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;


    @Bean("myJobLauncherJob")
    public Job myJobLauncherJob(){
        return jobBuilderFactory.get("myJobLauncherJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.myJobLauncherStep1())
                .build();

    }
    @Bean
    public Step myJobLauncherStep1(){
        // 任务Job传入的参数 实际上还是Step使用
        return stepBuilderFactory.get("myJobLauncherStep1")
                .listener(this) // 当前对象this实现了StepExecutionListener
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        // 使用参数
                        System.out.println(parameter.get("msg").getValue());
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
    private Map<String, JobParameter> parameter;
    @Override
    public void beforeStep(StepExecution stepExecution) {
        // 在Step执行之前 读取传入Job的参数 并存储起来 供Step中使用
        parameter = stepExecution.getJobParameters().getParameters();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
