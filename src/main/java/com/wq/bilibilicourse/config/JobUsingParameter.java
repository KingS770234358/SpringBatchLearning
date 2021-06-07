package com.wq.bilibilicourse.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


/**
 * 通过实现监听器的方式给Step传递参数
 */
@Configuration
@EnableBatchProcessing
public class JobUsingParameter implements StepExecutionListener {
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    private Map<String, JobParameter> parametersMap; // 用于存储参数的Map

    @Bean
    public Job jobWithParameter() {
        System.out.println("------createFlowJob------");
        return jobBuilderFactory.get("jobWithParameter")
                .start(this.parameterStep1())
                // .next(this.parameterStep2())
                .build();
    }
    // Job实际上执行的是Step，Job使用的数据肯定是在Step中使用
    // 只需要给Step传递参数即可，如何给Step传递参数？
    // 使用Step级别的监听方式（在 Step执行之前， 在Step执行之后， Step error）来传递数据
    @Bean
    public Step parameterStep1(){
        return stepBuilderFactory.get("parameterStep1")
                .listener(this)// this指向当前对象，当前对象实现了StepExecutionListener
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Job中的参数 info：" + parametersMap.get("info"));
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // 将Job的参数存入parametersMap中
        // 这里.getJobParameters() 可以取出 Job运行时的参数
        // 在IDEA中配置运行时参数
        parametersMap = stepExecution.getJobParameters().getParameters();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
