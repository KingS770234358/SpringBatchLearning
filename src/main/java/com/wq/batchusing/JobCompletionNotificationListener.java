package com.wq.batchusing;

import com.wq.pojo.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 当 批任务 完成过后收到通知
 */
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {
    // slf4j Logger 的使用
    public static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
//        System.out.println("jdbcTemplate" + jdbcTemplate.g);
        this.jdbcTemplate = jdbcTemplate; // 自动注入的jdbcTemplate
    }

    @Override
    public void afterJob(JobExecution jobExecution) { // 传入JobExecution，任务完成后的回调函数
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate.query("SELECT first_name, last_name FROM people", // 定义取数据的SQL语句
                    (rs, row) -> new Person( // rs为数据库中的1行
                            rs.getString(1),
                            rs.getString(2))
            ).forEach(person -> logger.info("Found <" + person + "> in the database."));
        }
    }
}
