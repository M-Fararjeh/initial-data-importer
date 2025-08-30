package com.importservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.importservice")
public class DataImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataImportApplication.class, args);
    }
}