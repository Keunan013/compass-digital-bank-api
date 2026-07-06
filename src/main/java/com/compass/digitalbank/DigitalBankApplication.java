package com.compass.digitalbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class DigitalBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankApplication.class, args);
    }
}
