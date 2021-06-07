package com.wq.bilibilicourse.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;

public class JobUsingParameterTest {
    public static void main(String[] args) {
        JobLauncher jobLauncher = new SimpleJobLauncher();
//        jobLauncher.run()
    }
}
