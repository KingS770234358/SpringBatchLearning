package com.wq.bilibilicourse.config.nestjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job的嵌套
 * 一个Job可以嵌套在另一个Job中，外部Job称为父Job，内部Job称为子Job
 * 子Job不能单独运行，需要由父Job启动
 */
@Configuration
@EnableBatchProcessing // 这两个注解可以放在启动类当中，就不用每次都写了
public class NestJob {
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    // △将两个子Job注入(使用相同的beanName)
    @Autowired
    public Job childJobOne;
    @Autowired
    public Job childJobTwo;
    // △注入Job的启动器对象
    @Autowired
    public JobLauncher launcher;

    /**
     * 父Job由2个子Job构成，
     * 两个子Job分别通过 this.childJobOne() 和 this.childJobTwo()
     * △需要传入 JobRepository 和 PlatformTransactionManager
     * @return
     */
    @Bean
    public Job parentJob(JobRepository repository, PlatformTransactionManager transactionManager){
        return jobBuilderFactory.get("parentJob")
                .start(this.childJobOne(repository,transactionManager))
                .next(this.childJobTwo(repository,transactionManager))
                .build();
    }

    public Step childJobOne(JobRepository repository, PlatformTransactionManager transactionManager) {
        // 通过JobStepBuilder(new StepBuilder("xxx"))将子Job转成Step
        return new JobStepBuilder(new StepBuilder("JobStepBuilder-StepBuilder-1"))
                .job(childJobOne) // 要转换成Step的子Job
                .launcher(launcher) // 使用 启动父Job的 JobLauncher 来启动 子Job
                .repository(repository) // 子Job 和 父Job 共用的 repository
                .transactionManager(transactionManager) // 子Job 和 父Job 共用的 transactionManager
                .build();
    }
    public Step childJobTwo(JobRepository repository, PlatformTransactionManager transactionManager) {
        return new JobStepBuilder(new StepBuilder("JobStepBuilder-StepBuilder-2"))
                .job(childJobTwo) // 要转换成Step的子Job
                .launcher(launcher) // 使用 启动父Job的 JobLauncher 来启动 子Job
                .repository(repository) // 子Job 和 父Job 共用的 repository
                .transactionManager(transactionManager) // 子Job 和 父Job 共用的 transactionManager
                .build();
    }

    @Bean
    public Step fatherStep1(){
        return stepBuilderFactory.get("fatherStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----fatherStep1----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step fatherStep2(){
        return stepBuilderFactory.get("fatherStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----fatherStep2----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
    @Bean
    public Step fatherStep3(){
        return stepBuilderFactory.get("fatherStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("----fatherStep3----");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
