package com.wq.bilibilicourse.config;

import com.wq.bilibilicourse.config.itemreaderl.ItemReaderImpl;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class RetryJob {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    private int tryTime = 0;
    @Bean
    public Job myRetryJob(){
        return jobBuilderFactory.get("myRetryJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.errorRetryStep1())
                .build();

    }
    @Bean
    public Step errorRetryStep1(){
        return stepBuilderFactory.get("errorRetryStep1")
                .<String, String>chunk(10)
                .reader(this.myItemReader4Retry())
                .processor(this.myItemProcessor4Retry())
                .writer(this.myItemWriter4Retry())
                .faultTolerant() // 开启容错功能，以支持“重试”
                .retry(CustomizationExcepion.class) // 指定在捕捉到 什么异常 的时候可以重试
                // 指定重试的次数 = reader重试次数 + processor重试次数 + writer重试次数
                // 该例子中可以通过设置重试次数来控制是否抛出异常 大于等于4次不会抛异常
                .retryLimit(5)
                .build();
    }
    @Bean
    public ItemReader<String> myItemReader4Retry(){
        List<String> items = new ArrayList<>();
        for (int i=0; i<60; i++){
            items.add(String.valueOf(i));
        }
        // 每次读取 0~59 这几个数据
        return new ListItemReader<>(items);
    }

    @Bean
    public ItemProcessor<String, String> myItemProcessor4Retry(){
        return new ItemProcessor<String, String>() {

            private int retryTime = 0;
            @Override
            public String process(String item) throws Exception {
                System.out.println("正在处理数据：" + item); // 整个批次中的10个数据都会重试
                if(item.equalsIgnoreCase("26")){
                    retryTime++;
                    if(retryTime<3){
                        System.out.println("处理数据：" + item + " " + retryTime + "times fail.");
                        throw new CustomizationExcepion("再重试看看...");
                    }
                    else{
                        System.out.println("处理数据：" + item + " " + retryTime + "times success.");
                        return String.valueOf(Integer.valueOf(item) * -1);
                    }

                }
                else return String.valueOf(Integer.valueOf(item) * -1); // 不是26则取反
            }
        };
    }
    @Bean
    public ItemWriter<String> myItemWriter4Retry(){
        return new ItemWriter<String>(){
            @Override
            public void write(List<? extends String> items) throws Exception {
                items.forEach( i -> System.out.println(i));
            }
        };
    }
}
