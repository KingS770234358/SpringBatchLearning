package com.wq.bilibilicourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
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
public class SkipListenerJob {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    private int tryTime = 0;
    @Bean
    public Job mySkipListenerJob(){
        return jobBuilderFactory.get("mySkipListenerJob")
                // jobExecutionDecider类似Flow和Step可以放在start(),from(),next(), to()当中
                .start(this.errorSkipListenerStep1())
                .build();

    }

    /**
     * 跳过的机制是：重头处理 该批数据 中的其他数据
     */
    @Bean
    public Step errorSkipListenerStep1(){
        return stepBuilderFactory.get("errorSkipStep1")
                .<String, String>chunk(10)
                .reader(this.myItemReader4SkipListener())
                .processor(this.myItemProcessor4SkipListener())
                .writer(this.myItemWriter4SkipListener())
                .faultTolerant() // 开启容错功能，以支持“跳过”
                .skip(CustomizationExcepion.class) // 指定在捕捉到 什么异常 的时候可以跳过
                .skipLimit(5) // reader processor writer中跳过这种异常的总次数
                .listener(this.mySkipListener())
                .build();
    }

    /**
     * 跳过异常 的监听器（重试也有监听器）
     */
    @Bean  //          输入 输出的数据类型
    public SkipListener<String, String> mySkipListener(){
        return new SkipListener<String, String>() {
            // reader中跳过处理过程中出现异常的数据
            @Override
            public void onSkipInRead(Throwable throwable) {
            }
            // writer中跳过处理过程中出现异常的数据
            @Override
            public void onSkipInWrite(String s, Throwable throwable) {
            }
            // processor中跳过处理过程中出现异常的数据
            @Override
            public void onSkipInProcess(String s, Throwable throwable) {
                System.out.println("处理过程中：" + s + "出现了异常：" + throwable);
            }
        };
    }
    @Bean
    public ItemReader<String> myItemReader4SkipListener(){
        List<String> items = new ArrayList<>();
        for (int i=0; i<60; i++){
            items.add(String.valueOf(i));
        }
        // 每次读取 0~59 这几个数据
        return new ListItemReader<>(items);
    }
    @Bean
    public ItemProcessor<String, String> myItemProcessor4SkipListener(){
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
    public ItemWriter<String> myItemWriter4SkipListener(){
        return new ItemWriter<String>(){
            @Override
            public void write(List<? extends String> items) throws Exception {
                items.forEach( i -> System.out.println(i));
            }
        };
    }
}
