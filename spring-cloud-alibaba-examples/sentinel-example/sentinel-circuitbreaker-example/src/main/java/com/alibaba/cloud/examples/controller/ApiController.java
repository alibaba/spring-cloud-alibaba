package com.alibaba.cloud.examples.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author freeman
 */
@RestController
public class ApiController {

    @GetMapping("/default/{ok}")
    public String defaultConfig(@PathVariable boolean ok) {
        if (ok) {
            return "ok";
        }
        throw new RuntimeException("fail");
    }

    @GetMapping("/feign/{ok}")
    public String feignConfig(@PathVariable boolean ok) {
        if (ok) {
            return "ok";
        }
        throw new RuntimeException("fail");
    }

    @GetMapping("/feignMethod/{ok}")
    public String feignMethodConfig(@PathVariable boolean ok) {
        if (ok) {
            return "ok";
        }
        throw new RuntimeException("fail");
    }

}
