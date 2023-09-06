package com.alibaba.cloud.consumer.feign.configuration;

import com.alibaba.cloud.consumer.feign.decorator.FeignClientDecorator;
import feign.Client;

import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Configuration
public class FeignConsumerConfiguration {

	@Bean
	public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
			SpringClientFactory clientFactory) {

		return new LoadBalancerFeignClient(
				new FeignClientDecorator(
						new Client.Default(null, null)),
				cachingFactory,
				clientFactory
		);
	}

}
