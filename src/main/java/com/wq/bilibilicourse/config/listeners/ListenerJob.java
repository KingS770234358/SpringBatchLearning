package com.wq.bilibilicourse.config.listeners;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableBatchProcessing
public class ListenerJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job JobWithlistener(){
        return jobBuilderFactory.get("JobWithlistener")
                .start(this.listenerChunckStep1())
                .listener(new MyJobListener()) // 注册自定义的JobExecutionListener实现
                .build();
    }

    @Bean
    public Step listenerChunckStep1(){
        return stepBuilderFactory.get("listenerChunckStep1")
                // 指定读取的数据类型和输出的数据类型
                .<String, String>chunk(2) // 每读x次数据输出1次 实现对数据的 读、处理、写
                .faultTolerant() // 容错
                .listener(new MyChunkListener()) // 配置自定义的 注解实现的ChunkListener
                .reader(this.read())// 定义从哪里读取出数据
                .writer(this.write())// 定义数据的输出
                .build();

    }

    /**
     * ItemReader的实现Bean
     * @return
     */
    @Bean            // 读取的是String类型的数据
    public ItemReader<String> read(){
        // ItemReader是1个接口，要返回1个它具体的实现，这里使用ListItemReader
        //      这里指定从哪里读出数据 Arrays.asList("JAVA", "Spring", "MyBatis")
        return new ListItemReader<>(Arrays.asList("JAVA", "Spring", "MyBatis"));
    }

    /**
     * ItemWriter的实现Bean
     * @return
     */
    @Bean
    public ItemWriter<String> write(){
        // ItemWriter是1个接口，要返回1个它具体的实现
        // 这里自定义实现ItemWriter接口
        return new ItemWriter<String>() {
            // 这里入参就是已经读取到的数据列表
            @Override
            public void write(List<? extends String> items) throws Exception {
                items.forEach( item -> System.out.println(item));
            }
        };
    }
}
