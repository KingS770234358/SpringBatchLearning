package com.wq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchProcessingApplication {
    public static void main(String[] args) {
        // 启动1个SpringBoot应用
        System.exit(SpringApplication.exit(SpringApplication.run(BatchProcessingApplication.class)));
    }
}
