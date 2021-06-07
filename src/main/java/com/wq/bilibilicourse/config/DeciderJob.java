package com.wq.bilibilicourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用决策期Decider实现比on更为强大的条件判断
 */
@Configuration
@EnableBatchProcessing
public class DeciderJob {
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    //创建决策器
    @Bean
    public JobExecutionDecider jobExecutionDecider(){
        return new MyDecider();
    }

    @Bean
    /**
     * JobExecutionDecider也是Job执行过程中的1个节点
     *                                       |-----> deciderStep3
     *                                      |               |
     * deciderStep1 -> jobExecutionDecider——  <----on(*)----
     *                                      |
     *                                      |-> deciderStep2 -> end()
     */
    public Job createDeciderJob(){
        return jobBuilderFactory.get("createDeciderJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.deciderStep1())
                .next(this.jobExecutionDecider())
                .from(this.jobExecutionDecider()).on("odd").to(this.deciderStep3())
                .from(this.deciderStep3()).on("*").to(this.jobExecutionDecider())
                .from(this.jobExecutionDecider()).on("even").to(this.deciderStep2())
                .end()// 包含 JobExecutionDecider 的也需要end()
                .build();

    }

    @Bean
    public Step deciderStep1(){
        return stepBuilderFactory.get("deciderStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----deciderStep1----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step deciderStep2(){
        return stepBuilderFactory.get("deciderStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----even----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step deciderStep3(){
        return stepBuilderFactory.get("deciderStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----odd----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
