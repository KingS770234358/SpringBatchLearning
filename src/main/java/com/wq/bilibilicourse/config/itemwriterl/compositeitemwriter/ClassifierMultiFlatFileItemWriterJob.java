package com.wq.bilibilicourse.config.itemwriterl.compositeitemwriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.classify.Classifier;
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
import java.util.function.Consumer;

@Configuration
@EnableBatchProcessing
public class ClassifierMultiFlatFileItemWriterJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // ????????????????????? ?????????????????????????????????ItemStreamWriter???????????????????????????writer
    @Autowired
    @Qualifier("myFlatFileItemWriter4ClassifierCompositeItemWriter")
    private ItemStreamWriter<Customer> myFlatFileWriter;
    @Autowired
    @Qualifier("myStaxEventItemWriter4ClassifierCompositeItemWriter")
    private ItemStreamWriter<Customer> myStaxEventItemWriter;

    @Bean
    public Job myClassifierMultiFlatFileItemWriterJob(){
        return jobBuilderFactory.get("myClassifierMultiFlatFileItemWriterJob")
                .start(this.myClassifierMultiFlatFileItemWriterStep1())
                .build();
    }
    @Bean
    public Step myClassifierMultiFlatFileItemWriterStep1(){
        return stepBuilderFactory.get("myMultiFlatFileItemWriterStep1")
                // ?????? ???????????????Customer ?????????Customer
                .<Customer, Customer>chunk(2) // 1????????????????????? ??????1???????????????
                .reader(this.myJDBCItemReader4ClassifierMultiFlatFileItemWriter())
                .writer(this.myClassifierCompositeItemWriter())
                // ??????myFlatFileWriter???myStaxEventItemWriter???????????????????????????...
                .stream(myFlatFileWriter)
                .stream(myStaxEventItemWriter)
                .build();
    }
    // ??????????????????
    @Bean
    public ClassifierCompositeItemWriter<Customer> myClassifierCompositeItemWriter(){
        ClassifierCompositeItemWriter<Customer> writer = new ClassifierCompositeItemWriter<>();
        // ??????????????????Classifier ??????classify??????
        writer.setClassifier(new Classifier<Customer, ItemWriter<? super Customer>>() {
            @Override
            public ItemWriter<? super Customer> classify(Customer customer) {
                // ??????Customer???id????????????
                ItemWriter<Customer> writer = null;
                writer = customer.getId()%2==0?
                        myFlatFileWriter
                        :myStaxEventItemWriter;
                return writer;
            }
        });
        return writer;
    }

    @Bean("myFlatFileItemWriter4ClassifierCompositeItemWriter")
    public FlatFileItemWriter<Customer> myFlatFileItemWriter4ClassifierCompositeItemWriter(){
        // ???Customer???????????????????????????????????????
        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<Customer>();
        // 1.?????????????????????????????????
        // ????????????ClassPathResource???????????????????????? ?????????????????????
        // writer.setResource(new ClassPathResource("customersfromysql.txt"));
        writer.setResource(new FileSystemResource("E:\\workspace\\springBatchLearning\\src\\main\\resources\\customers4ClassifierCompositeItemWriter.txt"));
        // 2.???????????????????????????Customer?????????????????????
        writer.setLineAggregator(new LineAggregator<Customer>() { // ????????????
            @Override
            public String aggregate(Customer item) {
                // 2.1??????ObjectMapper.writeValueAsString(Customer)???
                // ??????????????????????????????Customer??????????????? JSON ?????????
                ObjectMapper mapper = new ObjectMapper();
                String resultStr = null;
                try {
                    resultStr = mapper.writeValueAsString(item); //
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return resultStr; // 2.2?????? ?????????????????????????????????
            }
        });
        try {
            // 3. ??????FlatFileItemWriter???????????????
            writer.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer;
    }

    @Bean("myStaxEventItemWriter4ClassifierCompositeItemWriter")
    public StaxEventItemWriter<Customer> myStaxEventItemWriter4ClassifierCompositeItemWriter(){
        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();
        // ??????XstreamMarshaller ????????? ?????? xml???????????????
        // 1. ??????XstreamMarshaller ?????? xml??????????????? ??? ??? ???????????????
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);
        writer.setMarshaller(marshaller);
        // 2.???????????????xml?????????????????????????????????
        writer.setRootTagName("customers");
        // 3.????????????????????????xml???????????????
        writer.setResource(new FileSystemResource("E:\\workspace\\springBatchLearning\\src\\main\\resources\\customers4ClassifierCompositeItemWriter.xml"));
        try {
            // 4.writer?????????????????????????????????
            writer.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer;
    }

    @Autowired
    private DataSource dataSource;
    @Bean
    @StepScope // ??????????????????Step??????
    // ???Reader????????????Customer??????
    public JdbcPagingItemReader<Customer> myJDBCItemReader4ClassifierMultiFlatFileItemWriter(){
        // ??????Jdbc???????????????
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource); // ??????????????? application.properties?????????
        reader.setFetchSize(2); // ?????????????????????????????????========

        // ??????????????????Sql??????-MySqlPagingQueryProvider
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id,firstName,lastName,birthday"); // Sql?????????????????????????????????
        mySqlPagingQueryProvider.setFromClause("from customers"); // ?????????????????????????????????
        Map<String, Order> sortMap = new HashMap<>(1); // ??????????????? age ???1????????????????????? Map???????????????1??????
        sortMap.put("id", Order.DESCENDING); // ??????age??????????????????
        mySqlPagingQueryProvider.setSortKeys(sortMap);// ??????Provider???SQL?????????????????????
        reader.setQueryProvider(mySqlPagingQueryProvider);

        // ???????????????1????????? ?????????Customer??????
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                System.out.println("????????????" + rowNum);
                Customer customer = new Customer();
                customer.setId(resultSet.getInt(1)); // ???Int?????????????????????????????????1???????????????id
                customer.setFirstName(resultSet.getString(2));// ???String?????????????????????????????????2???????????????firstName
                customer.setLastName(resultSet.getString(3));
                customer.setBirthday(resultSet.getString(4));
                return customer;
            }
        });
        return reader;
    }
}
