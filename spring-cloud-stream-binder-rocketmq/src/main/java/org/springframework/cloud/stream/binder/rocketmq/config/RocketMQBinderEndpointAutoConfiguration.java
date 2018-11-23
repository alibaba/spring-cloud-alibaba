package org.springframework.cloud.stream.binder.rocketmq.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.stream.binder.rocketmq.actuator.RocketMQBinderEndpoint;
import org.springframework.cloud.stream.binder.rocketmq.actuator.RocketMQBinderHealthIndicator;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@AutoConfigureAfter(EndpointAutoConfiguration.class)
public class RocketMQBinderEndpointAutoConfiguration {

	@Bean
	public RocketMQBinderEndpoint rocketBinderEndpoint() {
		return new RocketMQBinderEndpoint();
	}

	@Bean
	public RocketMQBinderHealthIndicator rocketBinderHealthIndicator(
			InstrumentationManager instrumentationManager) {
		return new RocketMQBinderHealthIndicator(instrumentationManager);
	}

	@Bean
	public InstrumentationManager instrumentationManager(
			RocketMQBinderEndpoint rocketBinderEndpoint) {
		return new InstrumentationManager(rocketBinderEndpoint.metricRegistry(),
				rocketBinderEndpoint.runtime());
	}

}
