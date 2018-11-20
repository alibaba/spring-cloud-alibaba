package org.springframework.cloud.stream.binder.rocketmq.config;

import org.apache.rocketmq.client.log.ClientLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import org.springframework.cloud.stream.binder.rocketmq.consuming.ConsumersManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import org.springframework.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@EnableConfigurationProperties({ RocketMQBinderConfigurationProperties.class,
		RocketMQExtendedBindingProperties.class })
public class RocketMQBinderAutoConfiguration {

	private final RocketMQExtendedBindingProperties extendedBindingProperties;

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	@Autowired
	public RocketMQBinderAutoConfiguration(
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties) {
		this.extendedBindingProperties = extendedBindingProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		System.setProperty(ClientLogger.CLIENT_LOG_LEVEL,
				this.rocketBinderConfigurationProperties.getLogLevel());
	}

	@Bean
	public RocketMQTopicProvisioner provisioningProvider() {
		return new RocketMQTopicProvisioner();
	}

	@Bean
	public RocketMQMessageChannelBinder rocketMessageChannelBinder(
			RocketMQTopicProvisioner provisioningProvider,
			InstrumentationManager instrumentationManager,
			ConsumersManager consumersManager) {
		RocketMQMessageChannelBinder binder = new RocketMQMessageChannelBinder(
				consumersManager, extendedBindingProperties, provisioningProvider,
				rocketBinderConfigurationProperties, instrumentationManager);
		return binder;
	}

	@Bean
	public ConsumersManager consumersManager(
			InstrumentationManager instrumentationManager) {
		return new ConsumersManager(instrumentationManager,
				rocketBinderConfigurationProperties);
	}

}
