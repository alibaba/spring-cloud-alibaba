package com.alibaba.cloud.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.alibaba.cloud.examples.ConsumerApplication.EchoService;
import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;

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

	@LoadBalanced
	@Bean
	@SentinelRestTemplate
	public RestTemplate restTemplate1() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@FeignClient(name = "service-provider", fallback = EchoServiceFallback.class, configuration = FeignConfiguration.class)
	public interface EchoService {
		@RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
		String echo(@PathVariable("str") String str);

		@RequestMapping(value = "/divide", method = RequestMethod.GET)
		String divide(@RequestParam("a") Integer a, @RequestParam("b") Integer b);

		default String divide(Integer a) {
		    return divide(a, 0);
        }

		@RequestMapping(value = "/notFound", method = RequestMethod.GET)
		String notFound();
	}

}

class FeignConfiguration {
	@Bean
	public EchoServiceFallback echoServiceFallback() {
		return new EchoServiceFallback();
	}
}

class EchoServiceFallback implements EchoService {
	@Override
	public String echo(@PathVariable("str") String str) {
		return "echo fallback";
	}

	@Override
	public String divide(@RequestParam Integer a, @RequestParam Integer b) {
		return "divide fallback";
	}

	@Override
	public String notFound() {
		return "notFound fallback";
	}
}
