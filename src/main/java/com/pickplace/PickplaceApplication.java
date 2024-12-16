package com.pickplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class PickplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickplaceApplication.class, args);
    }
}