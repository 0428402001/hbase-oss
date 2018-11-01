package com.salmon.oss.core;

import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(DataSourceConfig.class)
@PropertySource("classpath:application.properties")
@ComponentScan(basePackageClasses = DataSourceConfig.class)
@MapperScan(basePackages = "com.salmon.oss.core.**.mapper", sqlSessionFactoryRef = "ossSqlSessionFactory")
public class BaseTest {

}
