package com.wq.bilibilicourse.config.itemreaderl;

import com.wq.bilibilicourse.config.listeners.MyJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Configuration
@EnableBatchProcessing
public class ItemReaderJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myItemReaderJob(){
        return jobBuilderFactory.get("itemReaderJob")
                .start(this.itemReaderStep1())
                .build();
    }
    @Bean
    public Step itemReaderStep1(){
        return stepBuilderFactory.get("itemReaderStep1")
                // 指定 输入 输出 的泛型
                .<String, String>chunk(20) // 1次读完几个数据 进行1次数据处理
                .reader(this.itemReadeeImpl()) // JdbcPagingItemReader分页从数据库读取
                .writer(list->{
                    list.forEach(i->{
                        System.out.println("***" + i + "***");
                    });
                })
                .build();
    }
    @Bean    // 根据上面配置的输入输出泛型，Reader读的是String
    public ItemReader<String> itemReadeeImpl(){
        List<String> data = Arrays.asList("cat","dog","pig","duck");
        return new ItemReaderImpl<String>(data);
    }
}
