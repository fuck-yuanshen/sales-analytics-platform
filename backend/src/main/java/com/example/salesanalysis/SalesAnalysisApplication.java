package com.example.salesanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SalesAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesAnalysisApplication.class, args);
    }
}
