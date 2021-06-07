package com.wq.bilibilicourse.config.nestjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class ChildJob {
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job childJobOne(){
        return jobBuilderFactory.get("childJobOne")
                .start(this.childStep1())
                .next(this.childStep2())
                .build();
    }

    @Bean
    public Job childJobTwo(){
        return jobBuilderFactory.get("childJobTwo")
                .start(this.childStep3())
                .build();
    }

    @Bean
    public Step childStep1(){
        return stepBuilderFactory.get("childStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----childJob1 - Step1----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step childStep2(){
        return stepBuilderFactory.get("childStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----childJob1 - Step2----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step childStep3(){
        return stepBuilderFactory.get("childStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----childJob2 - Step3----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
