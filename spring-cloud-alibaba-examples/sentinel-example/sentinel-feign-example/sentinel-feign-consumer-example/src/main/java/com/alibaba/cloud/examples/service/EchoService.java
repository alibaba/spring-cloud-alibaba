package com.alibaba.cloud.examples.service;

import com.alibaba.cloud.examples.fallback.EchoServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lengleng
 * @date 2019-08-01
 * <p>
 * example feign client
 */
@FeignClient(name = "service-provider", fallbackFactory = EchoServiceFallbackFactory.class)
public interface EchoService {

    /**
     * 调用服务提供方的输出接口
     *
     * @param str 用户输入
     * @return
     */
    @GetMapping(value = "/echo/{str}")
    String echo(@PathVariable("str") String str);
}
