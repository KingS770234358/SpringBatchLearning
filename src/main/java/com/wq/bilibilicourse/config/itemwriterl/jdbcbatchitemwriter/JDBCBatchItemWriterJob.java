package com.wq.bilibilicourse.config.itemwriterl.jdbcbatchitemwriter;

import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import com.wq.bilibilicourse.config.itemreaderl.jdbcitemreader.JDBCItemWriter;
import com.wq.bilibilicourse.config.itemreaderl.jdbcitemreader.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.BindException;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class JDBCBatchItemWriterJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myJDBCBatchItemWriterJob(){
        return jobBuilderFactory.get("myJDBCBatchItemWriterJob")
                .start(this.myJDBCBatchItemWriterStep1())
                .build();
    }

    @Autowired
    @Qualifier("jdbcItemWriter") // 指定要注入的Bean的BeanName
    public JDBCItemWriter jdbcItemWriter;
    @Bean
    public Step myJDBCBatchItemWriterStep1(){
        return stepBuilderFactory.get("myJDBCBatchItemWriterStep1")
                // 指定 输入读取成Customer 输出成Customer
                .<Customer, Customer>chunk(2) // 1次读完几个数据 进行1次数据处理
                .reader(this.flatFileItemReader4JDBCBatchWriter())
                .writer(this.myJDBCBatchItemWriter())
                .build();
    }
    // 1.设置要输出到的数据源
    @Autowired
    private DataSource dataSource;
    @Bean
    public JdbcBatchItemWriter<Customer> myJDBCBatchItemWriter(){
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<Customer>();
        // 1.设置要输出到的数据源
        writer.setDataSource(dataSource);
        // 2.指定SQL语句 插入到customers表（表名不能写错！
        writer.setSql("insert into customers (id, firstName, lastName, birthday) values" +
                "(:id, :firstName, :lastName, :birthday)"); // :占位符！！
        // 3.向SQL语句传递参数
        writer.setItemSqlParameterSourceProvider( // 将JAVA对象Customer中的属性都映射到占位符
                // 这个类型的provider是【按照对象的成员变量名】对上述占位符 进行属性填充
                new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }
    @Bean
    @StepScope
    public FlatFileItemReader<Customer> flatFileItemReader4JDBCBatchWriter(){
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        // reader.setResource(new ClassPathResource("xxx.txt"));
        //                 getClass().getResource("")
        reader.setResource(new ClassPathResource("customer4FlatFileItemReaderTest.txt"));
        reader.setLinesToSkip(1); // 设置跳过第几行

        // 解析数据 分词器
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(); // 行的“逗号”分词器
        tokenizer.setNames(new String[]{"id", "firstName","lastName","birthday"});// 将“列”分别设置字段名
        // 把解析出的数据映射成1个对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();
        mapper.setLineTokenizer(tokenizer);
        mapper.setFieldSetMapper(new FieldSetMapper<Customer>() {
            @Override                   // FieldSet类似ResultSet
            public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
                Customer customer = new Customer(); // 按上述设置的字段进行取值
                customer.setId(fieldSet.readInt("id")); // 以int类型读取出id字段
                customer.setFirstName(fieldSet.readString("firstName"));
                customer.setLastName(fieldSet.readString("lastName"));
                customer.setBirthday(fieldSet.readString("birthday"));
                return customer;
            }
        });
        // mapper定义（赋值）完之后 对mapper进行检查，如按逗号分隔之后的列数 与 setNames中的列数是否一致等
        mapper.afterPropertiesSet();
        // 将mapper设置到reader中
        reader.setLineMapper(mapper);
        // Reader -> (Line)Mapper ->  (Line)Tokenizer
        //                        ->  FieldSetMapper
        return reader;
    }
}
