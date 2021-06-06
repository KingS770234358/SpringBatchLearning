package com.wq.batchusing;

import com.wq.pojo.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 注意：这里引入的ItemProcessor所在的包
import org.springframework.batch.item.ItemProcessor;

/**
 * wire ... into 使...专注于...
 * upper-cased 大写
 * PersonItemProcessor实现了SpringBatch的ItemProcessor接口，使代码能够专注于 批任务；
 * 该接口接收一个Person对象作为输入，PersonItemProcessor将它的名字转换成大写。
 * （输出数据的类型和输出数据的类型可以不同）
 */
public class PersonItemProcessor implements ItemProcessor<Person, Person> {
    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }
}
