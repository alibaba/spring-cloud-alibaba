/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.examples;

import java.util.List;
import java.util.Random;

import com.alibaba.cloud.examples.ConsumerSCLBApplication.EchoService;
import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableDiscoveryClient(autoRegister = false)
@EnableFeignClients
public class ConsumerSCLBApplication {

	@LoadBalanced
	@Bean
	@SentinelRestTemplate(urlCleanerClass = UrlCleaner.class, urlCleaner = "clean")
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
		SpringApplication.run(ConsumerSCLBApplication.class, args);
	}

	@Configuration
	@LoadBalancerClient(value = "service-provider",
			configuration = MyLoadBalancerConfiguration.class)
	class MySCLBConfiguration {

	}

	static class RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

		private static final Logger log = LoggerFactory
				.getLogger(RandomLoadBalancer.class);

		private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

		private final String serviceId;

		private final Random random;

		RandomLoadBalancer(
				ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
				String serviceId) {
			this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
			this.serviceId = serviceId;
			this.random = new Random();
		}

		@Override
		public Mono<Response<ServiceInstance>> choose(Request request) {
			log.info("random spring cloud loadbalacer active -.-");
			ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
					.getIfAvailable(NoopServiceInstanceListSupplier::new);
			return supplier.get().next().map(this::getInstanceResponse);
		}

		private Response<ServiceInstance> getInstanceResponse(
				List<ServiceInstance> instances) {
			if (instances.isEmpty()) {
				return new EmptyResponse();
			}
			ServiceInstance instance = instances.get(random.nextInt(instances.size()));

			return new DefaultResponse(instance);
		}

	}

	@FeignClient(name = "service-provider", fallback = EchoServiceFallback.class,
			configuration = FeignConfiguration.class)
	public interface EchoService {

		@GetMapping("/echo/{str}")
		String echo(@PathVariable("str") String str);

		@GetMapping("/divide")
		String divide(@RequestParam("a") Integer a, @RequestParam("b") Integer b);

		default String divide(Integer a) {
			return divide(a, 0);
		}

		@GetMapping("/notFound")
		String notFound();

	}

	@RestController
	class TestController {

		@Autowired
		private RestTemplate restTemplate;

		@Autowired
		private RestTemplate restTemplate1;

		@Autowired
		private EchoService echoService;

		@Autowired
		private DiscoveryClient discoveryClient;

		@GetMapping("/echo-rest/{str}")
		public String rest(@PathVariable String str) {
			return restTemplate.getForObject("http://service-provider/echo/" + str,
					String.class);
		}

		@GetMapping("/echo-feign/{str}")
		public String feign(@PathVariable String str) {
			return echoService.echo(str);
		}

		@GetMapping("/index")
		public String index() {
			return restTemplate1.getForObject("http://service-provider", String.class);
		}

		@GetMapping("/test")
		public String test() {
			return restTemplate1.getForObject("http://service-provider/test",
					String.class);
		}

		@GetMapping("/sleep")
		public String sleep() {
			return restTemplate1.getForObject("http://service-provider/sleep",
					String.class);
		}

		@GetMapping("/notFound-feign")
		public String notFound() {
			return echoService.notFound();
		}

		@GetMapping("/divide-feign")
		public String divide(@RequestParam Integer a, @RequestParam Integer b) {
			return echoService.divide(a, b);
		}

		@GetMapping("/divide-feign2")
		public String divide(@RequestParam Integer a) {
			return echoService.divide(a);
		}

		@GetMapping("/services/{service}")
		public Object client(@PathVariable String service) {
			return discoveryClient.getInstances(service);
		}

		@GetMapping("/services")
		public Object services() {
			return discoveryClient.getServices();
		}

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
