package com.alibaba.cloud.stream.binder.rocketmq.binder;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;

import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 */
public class RocketMQTestBinder extends
		AbstractTestBinder<RocketMQMessageChannelBinder, ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>> {

	@Override
	public void cleanup() {
	}

	public RocketMQTestBinder(RocketMQTopicProvisioner provisioningProvider,
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			RocketMQProperties rocketMQProperties,
			InstrumentationManager instrumentationManager) {
		RocketMQMessageChannelBinder binder = new RocketMQMessageChannelBinder(
				provisioningProvider, extendedBindingProperties,
				rocketBinderConfigurationProperties, rocketMQProperties,
				instrumentationManager);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				Config.class);
		binder.setApplicationContext(context);
		binder.setApplicationEventPublisher(context);
		setBinder(binder);
	}

	@Configuration
	@EnableIntegration
	static class Config {

		@Bean
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

	}

}
