package com.wq.bilibilicourse.config.joblaunchertest;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobLauncherController {

    @Autowired
    private JobLauncher jobLauncher; // 由任务模块的@EnableBatchProcessing注解注册到容器中
    @Autowired // 任务模块JobLauncherJob的@Bean注册到容器中
    @Qualifier("myJobLauncherJob")
    private Job jobLauncherJob;
    @RequestMapping("/job/{msg}")
    public String jobRun(@PathVariable String msg){
        // 启动JobLauncher，把接收到的参数值，通过JobLauncher传给任务Job
        // 这里是JobParameters（JobParameter的集合）
        JobParameters parameter = new JobParametersBuilder()
                                    // 这里加入的是String，而不是addParameter()
                                    .addString("msg", msg)
                                    .toJobParameters();
        try {
            jobLauncher.run(jobLauncherJob, parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Job Success!";
    }
}
