package com.javaguy.nhx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NhxkesyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NhxkesyApplication.class, args);
    }

}
