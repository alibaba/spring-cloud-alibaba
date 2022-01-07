package com.alibaba.cloud.examples.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 *
 * @author freeman
 */
@FeignClient(value = "order", url = "http://localhost:${server.port}", fallback = OrderClientFallBack.class)
public interface OrderClient {

    @GetMapping("/default/{ok}")
    String defaultConfig(@PathVariable boolean ok);

}
