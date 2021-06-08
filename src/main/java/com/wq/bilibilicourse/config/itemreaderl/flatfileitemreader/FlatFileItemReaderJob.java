package com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.BindException;

@Configuration
@EnableBatchProcessing
public class FlatFileItemReaderJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job myFlatFileItemReaderJob(){
        return jobBuilderFactory.get("myFlatFileItemReaderJob")
                .start(this.myFlatFileItemReaderStep1())
                .build();
    }

    @Bean
    public Step myFlatFileItemReaderStep1(){
        return stepBuilderFactory.get("itemReaderStep1")
                // 指定 输入 输出 的泛型都为Customer对象
                .<Customer, Customer>chunk(3)
                .reader(this.flatFileItemReader())
                .writer(list->{
                    list.forEach(customer->{
                        System.out.println(customer);
                    });
                })
                .build();
    }
    @Bean
    @StepScope
    public FlatFileItemReader<Customer> flatFileItemReader(){
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
        return reader;
    }
}
