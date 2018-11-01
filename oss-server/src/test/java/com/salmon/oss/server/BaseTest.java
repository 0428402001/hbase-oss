package com.salmon.oss.server;

import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ServerStoreConfig.class)
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.salmon.oss.core", "com.salmon.oss.server"})
@MapperScan(basePackages = {"com.salmon.oss.**.mapper"}, sqlSessionFactoryRef = "ossSqlSessionFactory")
public class BaseTest {

}
