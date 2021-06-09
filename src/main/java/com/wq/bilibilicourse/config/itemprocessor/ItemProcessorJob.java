package com.wq.bilibilicourse.config.itemprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class ItemProcessorJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myItemProcessorJob(){
        return jobBuilderFactory.get("myItemProcessorJob")
                .start(this.myItemProcessorStep1())
                .build();
    }
    @Bean
    public Step myItemProcessorStep1(){
        return stepBuilderFactory.get("myItemProcessorStep1")
                // 指定 输入读取成Customer 输出成Customer
                .<Customer, Customer>chunk(2) // 1次读完几个数据 进行1次数据处理
                .reader(this.myJDBCItemReader4ItemProcessor())
                // .processor(this.myItemProcessor())
                .processor(this.myCompositeProcessor())
                .writer(this.myFlatFileItemWriter4ItemProcessor())
                .build();
    }

    @Bean
    public CompositeItemProcessor<Customer, Customer> myCompositeProcessor(){
        CompositeItemProcessor<Customer, Customer> processor = new CompositeItemProcessor<Customer, Customer>();
        List<ItemProcessor<Customer, Customer>> processors
                = Arrays.asList(this.myItemProcessor(), this.myItemProcessor1());
        processor.setDelegates(processors);
        return processor;
    }

    @Bean // 对ItemReader读入数据的处理 将Customer的firstName转成大写
    public ItemProcessor<Customer, Customer> myItemProcessor(){
        return new ItemProcessor<Customer, Customer>(){
            @Override
            public Customer process(Customer item) throws Exception {
                Customer customer = new Customer();
                customer.setId(item.getId());
                customer.setFirstName(item.getFirstName().toUpperCase());
                customer.setLastName(item.getLastName());
                customer.setBirthday(item.getBirthday());
                return customer; // 返回处理后的Customer
            }
        };
    }

    @Bean
    public ItemProcessor<Customer, Customer> myItemProcessor1(){
        return new ItemProcessor<Customer, Customer>(){
            @Override
            public Customer process(Customer item) throws Exception {
                if(item.getId()%2!=0){
                    return item;
                }else{
                    return null; // 过滤掉 id为偶数的 Customer
                }
            }
        };
    }

    @Bean
    public FlatFileItemWriter<Customer> myFlatFileItemWriter4ItemProcessor(){
        // 把Customer对象转换成字符串输出到文件
        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();
        // 1.设置写入的目标文件的路
        // 这里使用ClassPathResource定位目标输出文件 文件里没有内容
        // writer.setResource(new ClassPathResource("customersfromysql.txt"));
        writer.setResource(new FileSystemResource("E:\\workspace\\springBatchLearning\\src\\main\\resources\\customers4ItemProcessor.txt"));
        // 2.把从数据库读取中的Customer对象转成字符串
        writer.setLineAggregator(new LineAggregator<Customer>() { // 行聚合器
            @Override
            public String aggregate(Customer item) {
                // 2.1使用ObjectMapper.writeValueAsString(Customer)将
                // 从数据库中读取出来的Customer对象转换成 JSON 字符串
                ObjectMapper mapper = new ObjectMapper();
                String resultStr = null;
                try {
                    resultStr = mapper.writeValueAsString(item); //
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return resultStr; // 2.2返回 对象转换成的结果字符串
            }
        });
        try {
            // 3. 检查FlatFileItemWriter的属性设置
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
    public JdbcPagingItemReader<Customer> myJDBCItemReader4ItemProcessor(){
        // 创建Jdbc分页读取器
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource); // 设置数据源 application.properties中配置
        reader.setFetchSize(20); // 设置每次读取的记录条数

        // 指定查询用的Sql语句-MySqlPagingQueryProvider
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id,firstName,lastName,birthday"); // Sql语句中指定要查询的字段
        mySqlPagingQueryProvider.setFromClause("from customers"); // 指定从哪张表中读取数据
        Map<String, Order> sortMap = new HashMap<>(1); // 现在只根据 age 这1个字段进行排序 Map大小设置为1即可
        sortMap.put("id", Order.DESCENDING); // 根据age字段升序排序
        mySqlPagingQueryProvider.setSortKeys(sortMap);// 设置Provider中SQL语句的排序规则
        reader.setQueryProvider(mySqlPagingQueryProvider);

        // 将读取到的1条记录 映射成Customer对象
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                System.out.println(rowNum);
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
