package com.wq.bilibilicourse.config.itemreaderl.xmlstaxeventitemreader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import org.springframework.oxm.xstream.XStreamMarshaller;
import com.thoughtworks.xstream.XStream;

import java.util.HashMap;
import java.util.Map;

import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;

@Configuration
@EnableBatchProcessing
public class XmlItemReaderJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job myXmlItemReaderJob(){
        return jobBuilderFactory.get("myXmlItemReaderJob")
                .start(this.myXmlItemReaderStep1())
                .build();
    }
    @Bean
    public Step myXmlItemReaderStep1(){
        return stepBuilderFactory.get("myXmlItemReaderStep1")
                // 指定 输入 输出 的泛型都为Customer对象
                .<Customer, Customer>chunk(3)
                .reader(this.staxEvenItemReader())
                .writer(list->{
                    list.forEach(customer->{
                        System.out.println(customer);
                    });
                })
                .build();
    }
    @Bean
    @StepScope
    public StaxEventItemReader<Customer> staxEvenItemReader(){
        // 创建StaxEventItemReader
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();
        // 指定文件所在位置
        reader.setResource(new ClassPathResource("customers.xml"));
        // 指定要处理的目标跟标签 <customer> 而不是最上层的<customers>
        reader.setFragmentRootElementName("customer");
        // 将xml转换成Java对象 【需要maven依赖 spring-oxm 和 xstream】
        XStream xStream = new XStream();
        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypes(new Class[]{XmlItemReaderJob.class, Customer.class}); // 要关联的类
        XStreamMarshaller unmarshaller = new XStreamMarshaller();
        Map<String, Class> map = new HashMap<>();
        map.put("customer", Customer.class); // 将 “customer”标签 中的数据转成Customer的对象
        unmarshaller.setAliases(map); // 将 map 告知 unmarshaller，让它知道要进行什么样的转换
        // 将unmarshaller设置到reader中
        reader.setUnmarshaller(unmarshaller);
        return reader;
    }
}

