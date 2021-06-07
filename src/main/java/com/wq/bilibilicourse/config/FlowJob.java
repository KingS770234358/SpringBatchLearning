package com.wq.bilibilicourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ·Flow封装了多个Step，是Step的集合
 * ·使得多个Step可以被多个Job复用
 * ·使用构造函数new FlowBuilder<Flow>(flowName)创建Flow
 */
@Configuration
@EnableBatchProcessing
public class FlowJob {
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    /**
     * 创建任务Job
     * @return Job
     */
    @Bean
    public Job createFlowJob() {
        // 使用jobBuilderFactory创建人物
        System.out.println("------createFlowJob------");
        return jobBuilderFactory.get("createFlowJob")
                // start/from/next/to 都可以从 Flow开始执行
                .start(this.flowFlow())  // 从 xxxStep 开始
                .next(this.flowStep3())
                .end()// 包含Flow的Job需要end() 可以与SimpleJob.java中不含Flow的Job对比
                .build();
    }

    @Bean
    public Flow flowFlow(){
        // 与Job和Step不同，Flow直接通过构造函数new FlowBuilder<Flow>创建Flow
        return new FlowBuilder<Flow>("flowFlow")
                .start(this.flowStep1())
                .next(this.flowStep2())
                .build();
    }

    @Bean
    public Step flowStep1(){
        return stepBuilderFactory.get("flowStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----flowStep1----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step flowStep2(){
        return stepBuilderFactory.get("flowStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----flowStep2----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step flowStep3(){
        return stepBuilderFactory.get("flowStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----flowStep3----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
