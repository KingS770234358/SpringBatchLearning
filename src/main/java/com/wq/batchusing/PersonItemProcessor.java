package com.wq.batchusing;

import com.wq.pojo.Person; // pojo
import org.slf4j.*;
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
    // org.slf4j
    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    /**
     * 通过实现rg.springframework.batch.item.ItemProcessor接口的process
     * 对输入的数据进行 处理， 然后输出新的数据。（如上所述，输入输出的数据类型可以不同）
     * @param person 输入的待加工数据
     * @return 输出加工后得到的数据
     * @throws Exception
     */
    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();
        /**
         * 通过再创建1个对象的方式
         * 输入输出对象时两个完全不同的对象，而不是简单对原对象的值进行修改、
         */
        final Person transformedPerson = new Person(firstName, lastName);
        log.info("Converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson; // 返回加工后的数据
    }
}
