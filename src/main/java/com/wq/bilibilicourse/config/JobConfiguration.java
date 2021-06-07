package com.wq.bilibilicourse.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class JobConfiguration {
    // SpringBatch提供了许多实用类简化编码，使开发这可以专注于业务逻辑代码
    // SpringBatch在容器中注册了 JobBuilderFactory/StepBuilderFactory
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
    public Job createUserJob() {
        // 使用jobBuilderFactory创建人物
        return jobBuilderFactory.get("importUserJob") // get()方法定义批任务名
                .start(this.step1()) // step()方法可以传入Step对象 或者 Flow对象。
                .build(); // 正式的开始创建Job对象（上面都是对Job一些列的配置
    }

    @Bean // 添加Bean注解， 不能使用private修饰
    public Step step1() {
        // stepBuilderFactory定义Step对象
        return stepBuilderFactory.get("step1") // 定义Step的名称
                // Step的两种实现方式 tasklet/chunk
                .tasklet(new Tasklet() { // Tasklet是一个接口，要实现execute()方法
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("hello world");
                        // Step(tastlet)执行完之后 要返回1个状态RepeatStatus，用来决定后续要如何执行
                        // RepeatStatus：FINISHED / CONTINUABLE
                        return RepeatStatus.FINISHED;
                    }
                }).build();

    }
    /*
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob") // 批任务名
                .incrementer(new RunIdIncrementer()) // 自增器 JobParametersIncrementer 保证每次创建的jobInstance是不同的
                .listener(listener) // 注册监听器
                .flow(step1) // 执行“步骤”
                .end()// 结束任务
                .build();// 创建Job
    }
    */

    /*
    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")// reader的名称
                .resource(new ClassPathResource("sample-data.csv"))//reader要读取的数据来源-ClassPath下的资源
                .delimited()// 行分词器（以“，”为分隔符
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);// 把.csv中的数据源，转换成pojo Person
                }})
                .build();// 构建reader
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor(); // 自定义的Processor
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        // 使用SpringBoot默认的数据源 HikariDataSource
        // System.out.println("DataSource : " + dataSource);
        return new JdbcBatchItemWriterBuilder<Person>()
                // 组件SQL参数源提供者                 Bean属性的组件Sql参数 :firstName :lastName 提供者
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)") // sql语句
                .dataSource(dataSource)// 数据源
                .build(); // 构建JdbcBatchItemWriter
    }

     */
}
