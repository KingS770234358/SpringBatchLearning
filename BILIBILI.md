### 第一章 SpringBathc 入门

1.概述
1.1使用Java开发的，基于Spring框架的 轻量级、完善的 批处理框架，
包含大量可重用组件：日志、追踪、事务、任务作业统计、任务重启、跳过、重复、资源管理等。
对于**大数据量**和高性能要求的批处理任务，也提供了如 分区功能 远程功能 等高级功能和特性来支持

1.2SpringBatch是批处理应用框架！=调度框架，可与 调度框架 结合构建完整的批处理任务，它只关注
批处理任务的相关问题：事务、开发、监控、执行等，不提供调度功能；
常见的调度框架 Quartz、Tivoli、Control-M、Cron

1.3SpringBatch框架主要功能总结
· Transaction Management 事务管理
· Chunk based processing 基于块的处理
· Declare IO             声明式的输入输出
· Start/Stop/Restart     启动/停止/重启
· Retry/Skip             重试/跳过

1.4SpringBatch如何实现批处理
Job Launcher -> Job -> Step ->Item Reader
    |            |       |  ->Item Processor
    |            |       |  ->Item Writer
     ---Job Repository--- 
1个Job由多个Step构成；
Job由Job Launcher启动；
任务执行过程中的相关信息可以持久化到Job Repository中。

### 第二章 搭建SpringBatch项目
2.1 配置数据源
SpringBoot项目整合SpringBatch，由于SpringBatch是基于Job实现的，Job在执行
过程中的相关信息需要持久化到数据库中，所以会提示要配置“数据源”（H2/HSQL）

### 第三章 使用MySQL数据库进行持久化
当数据库中已经有某个Job的数据时，不能重新启动该Job。
Step already complete or not restartable,

### 第五集 SpringBatch核心API
Job Launcher -> Job -> Step ->Item Reader
    |            |       |  ->Item Processor
    |            |       |  ->Item Writer
     ---Job Repository--- 
 还有：
 JobParameter：给Job传参数，返回不同的JobInstance
 JobInstance：Job每一次执行都对应一个Instance。
 JobExecution
 JobExecutionContext
 StepExecution
 StepExecutionContext

/**
 * 问题1:在JobConfiguration中@Bean定义了两个同名的Step1，后者覆盖前者
 * 问题2:在pom.xml中添加mysql-connector-java和spring-boot-starter-jdbc依赖，
 *      版本不够，导不进来，导新的版本即可
 * 问题3: # always后面不能有空格
 *       spring.batch.initialize-schema=always
 * 问题4： 连接数据库url的配置中serverTimezone=CTT无法解析，改成UTC就好了
 * 问题5：多个配置文件配置多个job的时候，方法名会作为BeanName，
 *       所以Job和step的方法名都不能有重复的
 * 问题6：测试RepeatStatus.CONTINUABLE的时候直接关闭程序，
 *       改回RepeatStatus.FINISHED无法执行，需要数据库中原有的记录
 */
 
 ### 第六节 接听器的使用
 实现接口 & 使用注解
 JobExecutionListener(before, after)             Job的监听
 StepExecutionListener(before, after)            Step的监听
 ChunkListener(before, after, error)             如果Step使用Chunk实现
 ItemReadListener,ItemProcessListener,ItemWriteListener(before, after, error)
 
 ### 第三章
 #### 第1节
 使用Chunk方式实现Step的时候，可以使用ItemReader 和ItemWriter实现数据的输入输出
 ItemReader 和ItemWritem分别是实现数据输入和输出的接口
 自定义的ItemReaderImpl      读取String列表
 ListItemReader             读取列表
 JdbcPagingItemReader       读取MySQL
 FlatFileItemReader         读取普通文件
 StaxEventItemReader        读取xml文件
 MultiResourceItemReader    多文件读取数据
 
 #### 第2节
 处理在读取数据过程中发生的异常
 ItemStreamReader 继承 ItemReader 和 ItemStream
 ·ItemReader read()方法用于读取数据
 ·ItemStream 一系列方法用于处理异常
    open()    Step执行之前调用
    update()  Step【成功】处理完1批数据，Chunk(10),1批就是10条数据
    close()   Step全部执行完成之后执行
 示例：读取50行数据，在第37行，由于数据错误，导致抛出异常；
 Job停止后，将错误的数据改正，重启启动Job，将从上次出错的地方开始继续执行Job ----> 重启 
 batch_step_execution_context表中可以查看相关信息
 
 ### 第四章
 #### 第1节 ItemWriter数据概述
 ItemReader是逐条的读取数据，ItemWriter是逐批（chunck(10)定义）的输出数据
  自定义的MyItemWriter      输出String列表
  ·输出到数据库的接口
    Neo4jItemWriter
    MongoItemWriter
    RepositoryItemWriter
    HibernateItemWriter
    JdbcBatchItemWriter    √
    JpaItemWriter
    GemfireItemWriter
  任务：
  1.将resource目录下的customer4FlatFileItemReaderTest.txt中的数据
  读出，并输出到数据库中。
  customer4FlatFileItemReaderTest.txt
  2.将数据库中读取到的数据写入到指定txt文件中(要使用绝对路径定位文件系统中的文件进行输出
    使用ClassPathResource定位的话，任务结束后，目标文件中并没有数据)
  3.将数据库中读取到的数据写入到xml文件