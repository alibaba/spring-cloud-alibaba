package com.example.springbootb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpringBootBApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBApplication.class, args);
    }

}
