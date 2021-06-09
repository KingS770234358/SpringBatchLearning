package com.wq.bilibilicourse.config.joboperatortest;

import com.sun.deploy.cache.BaseLocalApplicationProperties;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableBatchProcessing
public class JobOperatorJob implements StepExecutionListener, ApplicationContextAware {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    //JobOperator需要配置JobLauncher，注入容器中注册的JobLauncher
    @Autowired
    private JobLauncher jobLauncher;
    //JobOperator需要配置JobRepository以持久化Job的信息，注入容器中框架注册的JobRepository
    @Autowired
    private JobRepository jobRepository;
    //JobOperator需要配置JobExplorer以管理Job执行过程中的相关资源，注入容器中框架注册的JobRepository
    @Autowired
    private JobExplorer jobExplorer;
    //JobOperator需要配置JobRegistry以关联JobOperator和指定的Job，注入容器中框架注册的JobRegistry
    @Autowired
    private JobRegistry jobRegistry;

    private ApplicationContext context;;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    /*
    * 必须要注册 JobRegistryBeanPostProcessor，否则会
    * 抛出异常：No job configuration with the name [myJobOperatorJob] was registered
    * 找不到jobOperator.start("myJobOperatorJob", "msg="+msg);指定的Job-"myJobOperatorJob"
    * */
    @Bean
    public JobRegistryBeanPostProcessor myJobRegistry() throws Exception {
        // JobRegistryBeanPostProcessor对框架注册的JobRegistry做了一定的分装处理
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();

        postProcessor.setJobRegistry(jobRegistry);
        postProcessor.setBeanFactory(context.getAutowireCapableBeanFactory());

        postProcessor.afterPropertiesSet(); // 检查属性设置
        return postProcessor;
    }

    @Bean
    public JobOperator myJobOperator(){
        // 1.创建JobOperator的实现对象
        SimpleJobOperator operator = new SimpleJobOperator(); // 最简单的Operator实现
        // 2.配置任务启动器JobLauncher
        operator.setJobLauncher(jobLauncher);
        // 3.配置参数转换器，将传入的 key=value形式的参数转换成可用的格式
        operator.setJobParametersConverter(new DefaultJobParametersConverter());
        // 4.配置Job执行过程中持久化的仓库
        operator.setJobRepository(jobRepository);
        // 5.配置JobExplorer Job的资源管理器 用于获取任务相关的信息，如：JobInstance，JobExecution等
        operator.setJobExplorer(jobExplorer);
        // 6.配置Job注册器，将JobOperator与jobOperator.start("myJobOperatorJob", "msg="+msg);
        // 中配置的Job-"myJobOperatorJob"关联起来
        operator.setJobRegistry(jobRegistry);
        return operator;
    }

    @Bean("myJobOperatorJob")
    public Job myJobOperatorJob(){
        return jobBuilderFactory.get("myJobOperatorJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.myJobOperatorStep1())
                .build();

    }

    private Map<String, JobParameter> parameter;
    @Bean
    public Step myJobOperatorStep1(){
        // 任务Job传入的参数 实际上还是Step使用
        return stepBuilderFactory.get("myJobLauncherStep1")
                .listener(this) // 当前对象this实现了StepExecutionListener
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        // 使用参数
                        System.out.println("---" + parameter.get("msg").getValue() + "---");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        parameter = stepExecution.getJobParameters().getParameters();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }


}
