package com.alibaba.cloud.examples.controller;

import com.alibaba.cloud.examples.feign.OrderClient;
import com.alibaba.cloud.examples.feign.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author freeman
 */
@RestController
public class TestController {

    @Autowired
    private UserClient userClient;
    @Autowired
    private OrderClient orderClient;


    @GetMapping("/test/default/{ok}")
    public String testDefault(@PathVariable boolean ok) {
        return orderClient.defaultConfig(ok);
    }

    @GetMapping("/test/feign/{ok}")
    public String testFeign(@PathVariable boolean ok) {
        return userClient.feign(ok);
    }

    @GetMapping("/test/feignMethod/{ok}")
    public String testFeignMethod(@PathVariable boolean ok) {
        return userClient.feignMethod(ok);
    }

}
