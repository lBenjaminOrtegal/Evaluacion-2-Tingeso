package com.tingeso.m2service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class M2ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(M2ServiceApplication.class, args);
    }

}
