package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ConsumerApplication {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }




    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @FeignClient(name = "service-provider")
    public interface EchoService {
        @RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
        String echo(@PathVariable("str") String str);
    }
}
