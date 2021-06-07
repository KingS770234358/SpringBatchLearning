### Batch processing with Spring
Job Launcher -> Job -> Step ->Item Reader
    |            |       |  ->Item Processor
    |            |       |  ->Item Writer
     ---Job Repository--- 

要实现的服务：从CSV的SpreadSheet中导入数据，使用自定义的代码进行转换，
最后把数据存入数据库（hsqldb 内存数据库）

1.创建CSV文件，待处理的数据
在csv text中
    Jill,Doe
    Joe,Doe
    Justin,Doe
    Jane,Doe
    John,Doe
2.接数据库
jdbc:mysql://localhost:3306/student?serverTimezone=CTT&amp;useUnicode=true&amp;characterEncoding=utf-8&amp;allowMultiQueries=true
3.创建表
DROP TABLE people IF EXISTS;
CREATE TABLE people  (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
4.配置Spring项目的pom文件（添加web支持）
        <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-batch</artifactId>
		</dependency>
		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.batch</groupId>
			<artifactId>spring-batch-test</artifactId>
			<scope>test</scope>
		</dependency>
5.开始编写代码
5.1创建pojo-person
-Lombok常用注解：
@Data 注解在类上；提供类所有属性的 getting 和 setting 方法，此外还提供了equals、canEqual、hashCode、toString 方法
@Setter ：注解在属性上；为属性提供 setting 方法
@Setter ：注解在属性上；为属性提供 getting 方法
@Log4j ：注解在类上；为类提供一个 属性名为log 的 log4j 日志对象
@NoArgsConstructor ：注解在类上；为类提供一个无参的构造方法
@AllArgsConstructor ：注解在类上；为类提供一个全参的构造方法
@Cleanup : 可以关闭流
@Builder ： 被注解的类加个构造者模式
@Synchronized ： 加个同步锁
@SneakyThrows : 等同于try/catch 捕获异常
@NonNull : 如果给参数加个这个注解 参数为null会抛出空指针异常
@Value : 注解和@Data类似，区别在于它会把所有成员变量默认定义为private final修饰，并且不会生成set方法。

6.slf4j Logger 的使用

7.数据源和数据库的区别
7.1数据源，如odbc数据源，是连接到实际数据库的一条路径，仅仅是数据库的连接名称，记录连接到哪个数据库以及如何连接，
其中并无真正的数据，一个数据库可以有多个数据源连接。
7.2SpringBoot中的数据源Hikari
Spring Boot 默认已经配置好了数据源Hikari DataSource，程序员可以直接 DI 注入然后使用即可
7.3HSQL
HSQL可以使用内存模式，将数据存储在内存中，SpringBoot通过Hikari连接HSQL进行数据传输




