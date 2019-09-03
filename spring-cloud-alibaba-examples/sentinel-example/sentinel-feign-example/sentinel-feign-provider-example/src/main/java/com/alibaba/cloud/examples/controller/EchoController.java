package com.alibaba.cloud.examples.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lengleng
 * @date 2019-08-01
 */
@RestController
public class EchoController {

    @GetMapping("/echo/{str}")
    public String echo(@PathVariable String str) {
        return "provider-" + str;
    }

}
