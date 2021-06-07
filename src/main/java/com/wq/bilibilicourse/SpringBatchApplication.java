package com.wq.bilibilicourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.wq.bilibilicourse"})
public class SpringBatchApplication {
    public static void main(String[] args) {
        /**
         * 问题1:在JobConfiguration中@Bean定义了两个同名的Step1，后者覆盖前者
         * 问题2:在pom.xml中添加mysql-connector-java和spring-boot-starter-jdbc依赖，
         *      版本不够，导不进来，导新的版本即可
         * 问题3: # always后面不能有空格
         *       spring.batch.initialize-schema=always
         * 问题4： 连接数据库url的配置中serverTimezone=CTT无法解析，改成UTC就好了
         * 问题5：多个配置文件配置多个job的时候，方法名会作为BeanName，
         *       所以Job和step的方法名都不能有重复的
         * 问题6：测试RepeatStatus.CONTINUABLE的时候直接关闭程序，
         *       改回RepeatStatus.FINISHED无法执行，需要数据库中原有的记录
         */
        SpringApplication.run(SpringBatchApplication.class);
    }
}
