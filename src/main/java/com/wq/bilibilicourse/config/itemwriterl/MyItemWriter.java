package com.wq.bilibilicourse.config.itemwriterl;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 与ItemReader相对的，ItemWriter只有1个writer方法
 * 简单的输出字符串的ItemWriter实现
 */
@Component("myItemWriter") // 作为组件Bean注入到容器中 供任务使用
public class MyItemWriter implements ItemWriter<String> {
    @Override
    public void write(List<? extends String> items) throws Exception {
        System.out.println(items.size()); // chunk(n) 这里输出的就是n
        for (String item : items) {
            System.out.println(item);
        }
    }
}
