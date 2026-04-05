package com.serene.sems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SemsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemsApplication.class, args);
    }
}
