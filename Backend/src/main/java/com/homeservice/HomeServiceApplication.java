package com.homeservice;

import com.homeservice.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeServiceApplication {

    public static void main(String[] args) {
        // Load .env before Spring context initializes
        DotenvLoader.load();
        SpringApplication.run(HomeServiceApplication.class, args);
    }
}
