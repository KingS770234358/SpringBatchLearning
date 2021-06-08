package com.wq.bilibilicourse.config.itemwriterl.xmlstaxeventitemwriter;

import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class StaxEventItemWriterJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myStaxEventItemWriterJob(){
        return jobBuilderFactory.get("myStaxEventItemWriterJob")
                .start(this.myStaxEventItemWriterStep1())
                .build();
    }
    @Bean
    public Step myStaxEventItemWriterStep1(){
        return stepBuilderFactory.get("myStaxEventItemWriterStep1")
                // 指定 输入读取成Customer 输出成Customer
                .<Customer, Customer>chunk(2) // 1次读完几个数据 进行1次数据处理
                .reader(this.myJDBCItemReader4StaxEventItemWriter())
                .writer(this.myStaxEventItemWriter())
                .build();
    }
    @Bean
    public StaxEventItemWriter<Customer> myStaxEventItemWriter(){
        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();
        // 使用XstreamMarshaller 将对象 转成 xml文件的元素
        // 1. 创建XstreamMarshaller 定义 xml次顶部标签 与 类 之间的关系
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);
        writer.setMarshaller(marshaller);
        // 2.设置生成的xml的最顶部（外部）的标签
        writer.setRootTagName("customers");
        // 3.设置要写入的目标xml的绝对路径
        writer.setResource(new FileSystemResource("E:\\workspace\\springBatchLearning\\src\\main\\resources\\customersfromysql.xml"));
        try {
            // 4.writer属性设置之后的检查工作
            writer.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer;
    }

    @Autowired
    private DataSource dataSource;
    @Bean
    @StepScope // 作用域规定在Step当中
    // 该Reader读取的是Customer对象
    public JdbcPagingItemReader<Customer> myJDBCItemReader4StaxEventItemWriter(){
        // 创建Jdbc分页读取器
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource); // 设置数据源 application.properties中配置
        reader.setFetchSize(2); // 设置每次读取的记录条数

        // 指定查询用的Sql语句-MySqlPagingQueryProvider
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id,firstName,lastName,birthday"); // Sql语句中指定要查询的字段
        mySqlPagingQueryProvider.setFromClause("from customers"); // 指定从哪张表中读取数据
        Map<String, Order> sortMap = new HashMap<>(1); // 现在只根据 age 这1个字段进行排序 Map大小设置为1即可
        sortMap.put("birthday", Order.ASCENDING); // 根据age字段升序排序
        mySqlPagingQueryProvider.setSortKeys(sortMap);// 设置Provider中SQL语句的排序规则
        reader.setQueryProvider(mySqlPagingQueryProvider);

        // 将读取到的1条记录 映射成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                Customer customer = new Customer();
                customer.setId(resultSet.getInt(1)); // 以Int的方式接收结果集中的第1列，也就是id
                customer.setFirstName(resultSet.getString(2));// 以String的方式接收结果集中的第2列，也就是firstName
                customer.setLastName(resultSet.getString(3));
                customer.setBirthday(resultSet.getString(4));
                return customer;
            }
        });
        return reader;
    }
}
