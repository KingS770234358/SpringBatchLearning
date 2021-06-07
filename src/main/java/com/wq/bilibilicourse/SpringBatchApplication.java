package com.wq.bilibilicourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.wq.bilibilicourse"})
public class SpringBatchApplication {
    public static void main(String[] args) {
        /**
         * 问题1:在JobConfiguration中@Bean定义了两个同名的Step1，后者覆盖前者
         */
        SpringApplication.run(SpringBatchApplication.class);
    }
}
