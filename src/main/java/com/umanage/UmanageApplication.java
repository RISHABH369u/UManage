package com.umanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UmanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmanageApplication.class, args);
    }
}
