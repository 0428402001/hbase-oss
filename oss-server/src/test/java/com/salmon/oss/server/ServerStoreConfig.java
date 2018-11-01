package com.salmon.oss.server;

import com.salmon.oss.server.hbase.HBaseServiceTemplate;
import com.salmon.oss.server.hdfs.HdfsService;
import com.salmon.oss.server.store.OssStoreService;
import com.salmon.oss.server.store.OssStoreServiceImpl;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;

@Configurable
public class ServerStoreConfig {

    @Autowired
    private Environment environment;

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
        ds.setUrl(environment.getProperty("spring.datasource.url"));
        ds.setUsername(environment.getProperty("spring.datasource.username"));
        ds.setPassword(environment.getProperty("spring.datasource.password"));
        return ds;
    }

    @Bean(name = "ossSqlSessionFactory")
    public SqlSessionFactory ossSqlSessionFactory(@Qualifier("dataSource") DataSource phoenixDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(phoenixDataSource);
        ResourceLoader loader = new DefaultResourceLoader();
        String resource = "classpath:mybatis-config.xml";
        factoryBean.setConfigLocation(loader.getResource(resource));
        factoryBean.setSqlSessionFactoryBuilder(new SqlSessionFactoryBuilder());
        return factoryBean.getObject();
    }

    @Bean
    public Connection getConnection() throws IOException {
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", environment.getProperty("hbase.zookeeper.quorum"));
        config.set("hbase.zookeeper.property.clientPort", environment.getProperty("hbase.zookeeper.port"));
        config.set(HConstants.HBASE_RPC_TIMEOUT_KEY, "3600000");
        return ConnectionFactory.createConnection(config);
    }

    @Bean(name = "ossStoreService")
    public OssStoreService getHosStore(@Autowired Connection connection,
                                       @Autowired HdfsService hdfsService,
                                       @Autowired HBaseServiceTemplate hBaseServiceTemplate) throws Exception {
        String zkHosts = environment.getProperty("hbase.zookeeper.quorum");
        OssStoreServiceImpl store = new OssStoreServiceImpl(connection, hdfsService, zkHosts, hBaseServiceTemplate);
        return store;
    }
}
