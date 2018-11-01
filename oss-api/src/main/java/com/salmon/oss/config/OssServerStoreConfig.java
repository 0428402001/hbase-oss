package com.salmon.oss.config;

import com.salmon.oss.server.hbase.HBaseServiceTemplate;
import com.salmon.oss.server.hdfs.HdfsService;
import com.salmon.oss.server.store.OssStoreService;
import com.salmon.oss.server.store.OssStoreServiceImpl;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OssServerStoreConfig {

    @Value("${hbase.zookeeper.quorum}")
    private String zkHosts;
    @Value("${hbase.zookeeper.port}")
    private String zkPorts;

    @Bean
    public Connection getConnection() throws IOException {
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", zkHosts);
        config.set("hbase.zookeeper.property.clientPort", zkPorts);
        config.set(HConstants.HBASE_RPC_TIMEOUT_KEY, "3600000");
        return ConnectionFactory.createConnection(config);
    }

    @Bean(name = "ossStoreService")
    public OssStoreService ossStoreService(@Autowired Connection connection,
                                           @Autowired HdfsService hdfsService,
                                           @Autowired HBaseServiceTemplate hBaseServiceTemplate) throws Exception {
        return new OssStoreServiceImpl(connection, hdfsService, zkHosts, hBaseServiceTemplate);
    }
}
