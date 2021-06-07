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
 * Split实现Flow的并发执行
 * 将3个Step放到2个Flow当中，这两个Flow再放到Job中，在Job中实现两个Flow的并发执行
 */
@Configuration
@EnableBatchProcessing
public class ParallelSplitJob {
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
    public Job createSplitJob() {
        // 使用jobBuilderFactory创建人物
        System.out.println("------createSplitJob------");
        return jobBuilderFactory.get("createSplitJob")
                // start/from/next/to 都可以从 Flow开始执行
                .start(this.splitFlow1())  // 从 xxxStep 开始
                .next(this.splitFlow2())
                .end()// 包含Flow的Job需要end() 可以与SimpleJob.java中不含Flow的Job对比
                .build();
    }

    @Bean
    public Flow splitFlow1(){
        // 与Job和Step不同，Flow直接通过构造函数new FlowBuilder<Flow>创建Flow
        return new FlowBuilder<Flow>("splitFlow1")
                .start(this.splitFlowStep1())
                .next(this.splitFlowStep2())
                .build();
    }
    @Bean
    public Flow splitFlow2(){
        // 与Job和Step不同，Flow直接通过构造函数new FlowBuilder<Flow>创建Flow
        return new FlowBuilder<Flow>("splitFlow2")
                .start(this.splitFlowStep3())
                .build();
    }
    @Bean
    public Step splitFlowStep1(){
        return stepBuilderFactory.get("splitFlowStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----splitFlowStep1----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step splitFlowStep2(){
        return stepBuilderFactory.get("splitFlowStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----splitFlowStep2----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step splitFlowStep3(){
        return stepBuilderFactory.get("splitFlowStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----splitFlowStep3----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
