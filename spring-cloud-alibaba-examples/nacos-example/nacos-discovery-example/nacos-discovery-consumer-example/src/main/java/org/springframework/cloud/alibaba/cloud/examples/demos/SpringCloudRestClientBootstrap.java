package org.springframework.cloud.alibaba.cloud.examples.demos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration // 激活自动装配
@EnableDiscoveryClient   // 激活服务注册和发现
@EnableFeignClients     //  激活 @FeignClients注册
public class SpringCloudRestClientBootstrap {

    @FeignClient("spring-cloud-alibaba-dubbo")
    public interface FeignEchoService {

        @GetMapping(value = "/echo")
        String echo(@RequestParam("message") String message);
    }


    @RestController
    public static class EchoServiceController {

        @Autowired
        private FeignEchoService feignEchoService;

        @GetMapping("/call/echo")
        public String echo(@RequestParam("message") String message) {
            return feignEchoService.echo(message);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudRestClientBootstrap.class, args);
    }
}
