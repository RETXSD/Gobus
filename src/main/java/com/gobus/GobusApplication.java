package com.gobus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GobusApplication {
    public static void main(String[] args) {
        SpringApplication.run(GobusApplication.class, args);
    }
}
