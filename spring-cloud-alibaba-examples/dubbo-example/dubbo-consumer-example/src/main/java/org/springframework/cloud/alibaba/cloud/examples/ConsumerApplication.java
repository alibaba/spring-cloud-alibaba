package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ConsumerApplication {
    @Bean
    @LoadBalanced
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



    @Reference(version = "1.0.0")
    private RestService restService;



    @Autowired
    private DubboFeignRestService dubboFeignRestService;


    @FeignClient("spring-cloud-alibaba-dubbo")
    @DubboTransported
    public interface DubboFeignRestService {

        @GetMapping(value = "/echo")
        String echo(@RequestParam String param);


    }
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);



    }

    @Bean
    public ApplicationRunner Runner() {
        return  args -> {
            System.out.println(restService.echo("hello"));
            System.out.println(restTemplate().getForEntity("http://spring-cloud-alibaba-dubbo/echo?param=hello",String.class));
            System.out.println(dubboFeignRestService.echo("hello"));
        };

    }



}
