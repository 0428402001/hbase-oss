package com.salmon.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ServletComponentScan(basePackages = "com.salmon.oss.listener")
public class ObjectStoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectStoreServiceApplication.class, args);
    }
}
