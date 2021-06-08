package com.wq.bilibilicourse.config.itemreaderl.jdbcitemreader;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

// 指定了BeanName
@Component("jdbcItemWriter") // 注入到容器中，供JDBCPagingReadItemReaderJob使用
public class JDBCItemWriter implements ItemWriter<User> {
    @Override
    public void write(List<? extends User> items) throws Exception {
        items.forEach(user -> {
            System.out.println(user);
        });
    }
}
