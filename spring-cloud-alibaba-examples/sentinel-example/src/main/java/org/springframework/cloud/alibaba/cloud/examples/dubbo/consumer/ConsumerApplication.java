package org.springframework.cloud.alibaba.cloud.examples.dubbo.consumer;

import java.util.Collections;

import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;

/**
 * @author fangjian
 */
@DubboComponentScan("org.springframework.cloud.alibaba.cloud.examples.dubbo.provider")
public class ConsumerApplication {

	@Bean
	public ApplicationConfig applicationConfig() {
		ApplicationConfig applicationConfig = new ApplicationConfig();
		applicationConfig.setName("demo-consumer");
		return applicationConfig;
	}

	@Bean
	public RegistryConfig registryConfig() {
		RegistryConfig registryConfig = new RegistryConfig();
		registryConfig.setAddress("multicast://224.5.6.7:1234");
		return registryConfig;
	}

	@Bean
	public ConsumerConfig consumerConfig() {
		ConsumerConfig consumerConfig = new ConsumerConfig();
		return consumerConfig;
	}

	@Bean
	public FooServiceConsumer annotationDemoServiceConsumer() {
		return new FooServiceConsumer();
	}

	public static void main(String[] args) {

		SpringApplicationBuilder consumerBuilder = new SpringApplicationBuilder()
				.bannerMode(Banner.Mode.OFF).registerShutdownHook(false)
				.logStartupInfo(false).web(false);
		ApplicationContext applicationContext = consumerBuilder
				.sources(ConsumerApplication.class).run(args);

		FlowRule flowRule = new FlowRule();
		flowRule.setResource(
				"org.springframework.cloud.alibaba.cloud.examples.dubbo.FooService:hello(java.lang.String)");
		flowRule.setCount(10);
		flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		flowRule.setLimitApp("default");
		FlowRuleManager.loadRules(Collections.singletonList(flowRule));

		FooServiceConsumer service = applicationContext.getBean(FooServiceConsumer.class);

		for (int i = 0; i < 15; i++) {
			try {
				String message = service.hello("Jim");
				System.out.println((i + 1) + " -> Success: " + message);
			}
			catch (SentinelRpcException ex) {
				System.out.println("Blocked");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
