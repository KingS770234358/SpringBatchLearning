package com.wq.bilibilicourse.config.joboperatortest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobOperatorController {

    @Autowired
    private JobOperator jobOperator; // 需要我们自己创建jobOperator对象注入到容器中

    @RequestMapping("/job2/{msg}")
    public String jobRun(@PathVariable String msg) throws Exception {

        //                   要启动的任务名       拼接方式传递参数 多个参数如何传递？
        jobOperator.start("myJobOperatorJob", "msg="+msg);
        return "Job Success!";
    }
}
