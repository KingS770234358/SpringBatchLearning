package com.wq.bilibilicourse.config.itemwriterl;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class ItemWriterJob{

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job myItemWriterJob(){
        return jobBuilderFactory.get("myItemWriterJob")
                .start(this.myItemWriterStep1())
                .build();
    }
    @Autowired
    @Qualifier("myItemWriter")
    private ItemWriter<String> myItemWriter;
    @Bean
    public Step myItemWriterStep1(){
        return stepBuilderFactory.get("itemReaderStep1")
                // 指定 输入 输出 的泛型
                .<String, String>chunk(20) // 1次读完几个数据 进行1次数据处理/输出
                .reader(this.myReaderW()) // JdbcPagingItemReader分页从数据库读取
                .writer(myItemWriter)
                .build();
    }
    @Bean
    public ItemReader myReaderW(){
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add("Java" + i);
        }
        return new ListItemReader<String>(items);
    }

}
