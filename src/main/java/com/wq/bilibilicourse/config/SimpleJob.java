package com.wq.bilibilicourse.config;

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
public class SimpleJob {
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
    public Job createSimpleJob() {
        // 使用jobBuilderFactory创建人物
        System.out.println("------createSimpleJob------");
        return jobBuilderFactory.get("createSimpleJob")
                .start(this.simpleJobStep1())  // 从 xxxStep 开始
                .next(this.simpleJobStep2())   // 接着执行 xxxStep
                .next(this.simpleJobStep3())   // 接着执行 xxxStep
                .build(); // 创建Job
    }

    @Bean
    public Job createSimpleConditionalJob() {
        // 使用jobBuilderFactory创建人物
        System.out.println("------createSimpleConditionalJob------");
        return jobBuilderFactory.get("createSimpleConditionalJob")
                // start为开始Job，on当满足 simpleJobStep1完成 的条件，to执行 simpleJobStep2
                // to 执行 、 fail 直接失败/下一个方法无法执行 、 stopAndRestart 重新执行
                .start(this.simpleJobStep1()).on("COMPLETED").to(this.simpleJobStep2())
                // from为继上1个Step，当满足xxxx，执行xxxx
                .from(this.simpleJobStep2()).on("COMPLETED").to(this.simpleJobStep3())
                // from ... end  继Step XXX 之后 Job结束
                .from(this.simpleJobStep3()).end()
                .build(); // 创建Job
    }

    @Bean
    public Step simpleJobStep1(){
        return stepBuilderFactory.get("step1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("------step1 executing------");
                        return RepeatStatus.FINISHED; // 只有当前任务FINISHED才会继续执行下一个step
                    }
                }).build();
    }
    @Bean
    public Step simpleJobStep2(){
        return stepBuilderFactory.get("step2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("------step2 executing------");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step simpleJobStep3(){
        return stepBuilderFactory.get("step3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("------step3 executing------");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
