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
  4.将数据库中读取到的数据写入到多个文件中
 #### 第2节 ItemProcessor的使用
 ItemProcessor<I,O>用于处理业务逻辑，验证，过滤等功能
 CompositeItemProcessor
 从数据库读取数据 用处理器处理 然后输出到文件

 #### 第3节 项目启动后Job不自动执行，而是在指定的时机通过JobLauncher启动
 1.在pom.xml中添加 web的starter
 2.编写前端页面， resource/static/index.xml，这样启动SpringBoot Web项目之后直接可以访问到该页面
 3.application.properties文件中要设置默认不自动执行Job
 4.编写controller，在接收到请求的时候用JobLauncher启动自定义的任务，并在Step执行过程中输出传入Job的参数
 @Autowired
 private JobLauncher jobLauncher; // 由任务模块的@EnableBatchProcessing注解注册到容器中
 @Autowired // 任务模块JobLauncherJob的@Bean注册到容器中
 @Qualifier("myJobLauncherJob")
 private Job jobLauncherJob;
 @RequestMapping("/job/{msg}")
 public String jobRun(@PathVariable String msg){
     // 启动JobLauncher，把接收到的参数值，通过JobLauncher传给任务Job
     // 这里是JobParameters（JobParameter的集合）
     JobParameters parameter = new JobParametersBuilder()
                                 // 这里加入的是String，而不是addParameter()
                                 .addString("msg", msg)
                                 .toJobParameters();
     try {
         jobLauncher.run(jobLauncherJob, parameter);
     } catch (Exception e) {
         e.printStackTrace();
     }
     return "Job Success!";
 }
 5.当Job传入的参数与之前传入的参数都不同的时候，可以视为Job不同的执行；
   当Job传入的参数与之前某次传入的参数相同的时候，抛出异常：
   A job instance already exists and is complete for parameters={msg=22222}.  
   If you want to run this job again, change the parameters.
   
  #### 第4节 JobOperator封装JobLauncher启动任务
  1.新增页面按钮和请求发送script
  2.新增Controller进行 URL映射
  3.创建JobOperator并进行相关配置，然后注入到容器中，包括：
    jobLauncher jobRepository jobExplorer jobRegistry
  4.创建JobRegistryBeanPostProcessor对JobRegistry进行相关处理，然后注入到容器中。
    其中需要获得context.getAutowireCapableBeanFactory()
    如果没有注册JobRegistryBeanPostProcessor会抛出异常：
    No job configuration with the name [myJobOperatorJob] was registered
    找不到jobOperator.start("myJobOperatorJob", "msg="+msg);指定的Job-"myJobOperatorJob"
  5.当Job传入的参数与之前传入的参数都不同的时候，可以视为Job不同的执行；
     当Job传入的参数与之前某次传入的参数相同的时候，抛出异常：
     A job instance already exists and is complete for parameters={msg=22222}.  
     If you want to run this job again, change the parameters.
 
 ### Extra JobExecution JobExecutionContext StepExecution StepExecutionContext
 https://blog.csdn.net/masson32/article/details/110411318