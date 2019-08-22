package com.alibaba.cloud.examples;

import java.util.Collections;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * @author fangjian
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.cloud.examples")
public class SentinelDubboConsumerApp {

	@Bean
	public FooServiceConsumer annotationDemoServiceConsumer() {
		return new FooServiceConsumer();
	}

	public static void main(String[] args) {

		FlowRule flowRule = new FlowRule();
		flowRule.setResource("com.alibaba.cloud.examples.FooService:hello(java.lang.String)");
		flowRule.setCount(10);
		flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		flowRule.setLimitApp("default");
		FlowRuleManager.loadRules(Collections.singletonList(flowRule));

		SpringApplicationBuilder consumerBuilder = new SpringApplicationBuilder();
		ApplicationContext applicationContext = consumerBuilder
				.web(WebApplicationType.NONE).sources(SentinelDubboConsumerApp.class)
				.run(args);

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
