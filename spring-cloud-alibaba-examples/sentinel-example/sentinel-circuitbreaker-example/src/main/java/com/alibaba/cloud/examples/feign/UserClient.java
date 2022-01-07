package com.alibaba.cloud.examples.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 *
 * @author freeman
 */
@FeignClient(value = "user", url = "http://localhost:${server.port}", fallback = UserClientFallBack.class)
public interface UserClient {

    @GetMapping("/feignMethod/{ok}")
    String feignMethod(@PathVariable boolean ok);

    @GetMapping("/feign/{ok}")
    String feign(@PathVariable boolean ok);

}
