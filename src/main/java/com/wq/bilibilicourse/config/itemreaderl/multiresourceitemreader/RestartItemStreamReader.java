package com.wq.bilibilicourse.config.itemreaderl.multiresourceitemreader;

import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.Customer;
import com.wq.bilibilicourse.config.itemreaderl.flatfileitemreader.FlatFileItemReaderJob;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Component("restartItemStreamReader") // 注册到Spring容器当中 注入到Job中供Job使用
public class RestartItemStreamReader implements ItemStreamReader<Customer> {

    // 内部还是使用FlatFileItemReader进行单个文件的读取
    private FlatFileItemReader<Customer> customerFlatFileItemReader = new FlatFileItemReader<>();
    private Long curLine = 0L; // 用于记录当前读取到的位置
    private boolean restart = false;
    private ExecutionContext executionContext; // 通过ExecutionContext向数据库中持久化信息

    public RestartItemStreamReader(){
        // 构造函数中主要是对FlatFileReader的初始化
        // 1. 设置要读取的文件
        customerFlatFileItemReader.setResource(new ClassPathResource("restartitemstreamreader.txt"));
        // 2. 解析数据的分词器
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // 3. 定制四个表头字段 定义列名
        tokenizer.setNames(new String[]{"id", "firstName", "lastName", "birthday"});
        // 4. 把1行映射为1个Customer对象
        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();
        mapper.setLineTokenizer(tokenizer); // 3->4
        mapper.setFieldSetMapper(new FieldSetMapper<Customer>() {
            @Override
            public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
                Customer customer = new Customer();
                customer.setId(fieldSet.readInt("id")); // 按列名读取
                customer.setFirstName(fieldSet.readString("firstName"));
                customer.setLastName(fieldSet.readString("lastName"));
                customer.setBirthday(fieldSet.readString("birthday"));
                return customer;
            }
        });
        mapper.afterPropertiesSet();
        customerFlatFileItemReader.setLineMapper(mapper);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        // 通过ExecutionContext中是否有 curLine这个键 来判断是不是重启任务
        if(executionContext.containsKey("curLine")){
            this.curLine = executionContext.getLong("curLine"); // 从ExecutionContext持久化的数据库中读取当前位置
            this.restart = true; // 如果executionContext中有 curLine这个键，说明之前已经启动过，本次是重启任务
        }else{
            this.curLine = 0L; // executionContext中没有 curLine这个键，本次是第一次启动任务
            executionContext.put("curLine", this.curLine);
            System.out.println("Start reading from line: " + this.curLine + 1);
        }
    }

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        this.curLine++; // 读取1条记录 =====> reader 1次只读1条数据；chunck 1次读多条数据
        Customer customer = null;
        if(restart){ // restart为true说明本次启动任务是重启任务  -  本次读取1条记录是重启后的第一条
            // 跳过已读过的行数，重定位读取开始位置
            customerFlatFileItemReader.setLinesToSkip(this.curLine.intValue() - 1);
            restart = false;// 重置重启标志  -  下一条记录的读取 才不会再跳行
            System.out.println("Restart reading from line:" + this.curLine);
        }
        customerFlatFileItemReader.open(this.executionContext); // 开启executionContext用于持久化
        customer = customerFlatFileItemReader.read();
        if(customer!=null && customer.getFirstName().equals("WrongName")){ // 自定义抛出异常 用于测试
            // java.lang.RuntimeException: Something wrong, Customer Id: 37
            throw new RuntimeException("Something wrong, Customer Id: " + customer.getId());
            // 抛出异常的时候，在数据库中存储
            // {"@class":"java.util.HashMap",
            // "batch.taskletType":"org.springframework.batch.core.step.item.ChunkOrientedTasklet",
            // "curLine":["java.lang.Long",36],
            // "batch.stepType":"org.springframework.batch.core.step.tasklet.TaskletStep"}
        }
        return customer;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 更新ExecutionContext中的当前行数位置，持久化到数据库，供重启的时候重新读取 恢复读取位置、
        executionContext.put("curLine", this.curLine);
        System.out.println("currentLine:" + this.curLine);
        // 全部读取完之后在数据库中的存储
        // {"@class":"java.util.HashMap",
        // "batch.taskletType":"org.springframework.batch.core.step.item.ChunkOrientedTasklet",
        // "curLine":["java.lang.Long",51],
        // "batch.stepType":"org.springframework.batch.core.step.tasklet.TaskletStep"}
    }

    @Override
    public void close() throws ItemStreamException {
        // 整个Job结束之后调用close()方法
        System.out.println("Work finished!");
    }
}
