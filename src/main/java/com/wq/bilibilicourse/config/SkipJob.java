package com.wq.bilibilicourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class SkipJob {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    private int tryTime = 0;
    @Bean
    public Job mySkipJob(){
        return jobBuilderFactory.get("mySkipJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.errorSkipStep1())
                .build();

    }

    /**
     * 跳过的机制是：重头处理 该批数据 中的其他数据
     */
    @Bean
    public Step errorSkipStep1(){
        return stepBuilderFactory.get("errorSkipStep1")
                .<String, String>chunk(10)
                .reader(this.myItemReader4Skip())
                .processor(this.myItemProcessor4Skip())
                .writer(this.myItemWriter4Skip())
                .faultTolerant() // 开启容错功能，以支持“跳过”
                .skip(CustomizationExcepion.class) // 指定在捕捉到 什么异常 的时候可以跳过
                .skipLimit(5) // reader processor writer中跳过这种异常的总次数
                .build();
    }
    @Bean
    public ItemReader<String> myItemReader4Skip(){
        List<String> items = new ArrayList<>();
        for (int i=0; i<60; i++){
            items.add(String.valueOf(i));
        }
        // 每次读取 0~59 这几个数据
        return new ListItemReader<>(items);
    }
    @Bean
    public ItemProcessor<String, String> myItemProcessor4Skip(){
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
    public ItemWriter<String> myItemWriter4Skip(){
        return new ItemWriter<String>(){
            @Override
            public void write(List<? extends String> items) throws Exception {
                items.forEach( i -> System.out.println(i));
            }
        };
    }
}
