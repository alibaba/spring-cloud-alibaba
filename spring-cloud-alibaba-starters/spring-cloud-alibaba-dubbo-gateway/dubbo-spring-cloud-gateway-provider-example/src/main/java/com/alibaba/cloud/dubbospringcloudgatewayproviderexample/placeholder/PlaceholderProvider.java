package com.alibaba.cloud.dubbospringcloudgatewayproviderexample.placeholder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/placeholder")
public class PlaceholderProvider {

    @GetMapping("/provider")
    public String providerName() {
        return "This is a placeholder provider for Dubbo !!";
    }

    // /someName?name={item}
    @GetMapping("/someName")
    @ResponseBody
    public String pathVariables(@RequestParam String name) {
        if (!name.isEmpty()) {
            return "The name is " + name;
        }

    return "Name not found !!";
    }
}
