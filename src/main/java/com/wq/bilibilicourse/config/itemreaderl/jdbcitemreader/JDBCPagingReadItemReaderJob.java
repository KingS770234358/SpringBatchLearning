package com.wq.bilibilicourse.config.itemreaderl.jdbcitemreader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class JDBCPagingReadItemReaderJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myJDBCItemReaderJob(){
        return jobBuilderFactory.get("myJDBCItemReaderJob")
                .start(this.jdbcItemReaderStep1())
                .build();
    }

    @Autowired
    @Qualifier("jdbcItemWriter") // 指定要注入的Bean的BeanName
    public JDBCItemWriter jdbcItemWriter;
    @Bean
    public Step jdbcItemReaderStep1(){
        return stepBuilderFactory.get("jdbcItemReaderStep1")
                // 指定 输入User 输出User
                .<User, User>chunk(2) // 1次读完几个数据 进行1次数据处理
                .reader(this.myJDBCItemReader()) // reader使用自己实现的JdbcPagingItemReader
                .writer(jdbcItemWriter)// 直接设置Writer为上述注入的jdbcItemWriter
                .build();
    }

    @Autowired // 将容器中的DataSource对象注入，用于设置JdbcPagingItemReader的数据源
    private DataSource dataSource;
    @Bean
    @StepScope // 作用域规定在Step当中
    // 该Reader读取的是User对象
    public JdbcPagingItemReader<User> myJDBCItemReader(){
        // 创建Jdbc分页读取器
        JdbcPagingItemReader<User> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource); // 设置数据源 application.properties中配置
        reader.setFetchSize(2); // 设置每次读取的记录条数

        // 指定查询用的Sql语句-MySqlPagingQueryProvider
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id,username,password,age"); // Sql语句中指定要查询的字段
        mySqlPagingQueryProvider.setFromClause("from user"); // 指定从哪张表中读取数据
        Map<String, Order> sortMap = new HashMap<>(1); // 现在只根据 age 这1个字段进行排序 Map大小设置为1即可
        sortMap.put("age", Order.ASCENDING); // 根据age字段升序排序
        mySqlPagingQueryProvider.setSortKeys(sortMap);// 设置Provider中SQL语句的排序规则
        reader.setQueryProvider(mySqlPagingQueryProvider);

        // 将读取到的1条记录 映射成User对象
        reader.setRowMapper(new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                User user = new User();
                user.setId(resultSet.getInt(1)); // 以Int的方式接收结果集中的第1列，也就是id
                user.setUsername(resultSet.getString(2));// 以String的方式接收结果集中的第2列，也就是username
                user.setPassword(resultSet.getString(3));
                user.setAge(resultSet.getInt(4));
                return user;
            }
        });
        return reader;
    }
}
