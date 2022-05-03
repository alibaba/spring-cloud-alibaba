package com.example.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/index")
    public String index() {
        return "Greetings from Spring Boot!\n";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!\n";
    }

}
