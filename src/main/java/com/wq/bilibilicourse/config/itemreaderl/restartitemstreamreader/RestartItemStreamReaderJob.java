package com.wq.bilibilicourse.config.itemreaderl.restartitemstreamreader;

import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import com.wq.bilibilicourse.config.itemreaderl.multiresourceitemreader.RestartItemStreamReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class RestartItemStreamReaderJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job myRestartItemStreamReaderJob(){
        return jobBuilderFactory.get("myRestartItemStreamReaderJob")
                .start(this.myRestartItemStreamReaderStep1())
                .build();
    }
    @Autowired // @Qualifier必须配合@Autowired使用，否则报错
    @Qualifier("restartItemStreamReader")
    private RestartItemStreamReader restartItemStreamReader;
    @Bean
    public Step myRestartItemStreamReaderStep1(){
        return stepBuilderFactory.get("myRestartItemStreamReaderStep1")
                // 指定 输入 输出 的泛型都为Customer对象
                .<Customer, Customer>chunk(8) // 当chunk其中1条数据读取失败，不会触发update()方法
                .reader(this.restartItemStreamReader)
                .writer(list->{
                    list.forEach(customer->{
                        System.out.println(customer);
                    });
                })
                .build();
    }
}
