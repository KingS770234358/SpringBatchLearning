package com.wq.batchusing;


import com.wq.pojo.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import javax.xml.crypto.Data;


/**
 *  legwork 跑腿活
 *  将实际的批任务整合起来
 */
// Spring配置类，通过@Config注解注入到容器中
@Configuration
/**
 * @EnableBatchProcessing 注解将许多支持 任务处理的bean添加到容器中，免去跑腿活。
 * 示例中使用基于内存的数据库HSQL（由 @EnableBatchProcessing提供）
 */
@EnableBatchProcessing
public class BatchConfig {
    // SpringBatch提供了许多实用类简化编码，使开发这可以专注于业务逻辑代码
    // 1.任务工厂 JobBuilderFactory
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    // 2.步骤工厂 StepBuilderFactory
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    /**
     * 3实际的任务配置
     * @param listener 批任务执行过程中的监听器
     * @param step1 组成批任务的“步骤元素”
     * @return 配置好的批任务
     */
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob") // 批任务名
                .incrementer(new RunIdIncrementer()) // 自增器 JobParametersIncrementer 保证每次创建的jobInstance是不同的
                .listener(listener) // 注册监听器
                .flow(step1) // 执行“步骤”
                .end()// 结束任务
                .build();// 创建Job
    }

    /**
     * 4.要经过的步骤，这里以chunk实现（也可以以tasklet实现）
     * @param writer
     * @return
     */
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) { // 传入1个JdbcBatchItemWriter定义step
        return stepBuilderFactory.get("step1")// step的名称
                .<Person, Person> chunk(10) // 以Chunk的方式实现step，也可以用tasklet的方式实现
                .reader(this.reader()) // 数据读取器
                .processor(this.processor()) // 中间处理器
                .writer(writer)// 负责数据写入的JdbcBatchItemWriter
                .build(); // 构建step
    }

    /**
     * 查找以“sample-data.csv”命名的文件，将每行解析成1个Person对象。
     * @return 返回FlatFileItemReader<Person>
     */
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

    /**
     * 以JDBC为写入目标，自动获取由@EnableBatchProcessing提供的dataSource副本。
     * 包括 向数据库插入1条记录所需要的参数，由Java Bean属性驱动
     * @param dataSource
     * @return
     */
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
}
