package org.springframework.cloud.stream.binder.rocketmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.rocketmq.consuming.ConsumersManager;
import org.springframework.cloud.stream.binder.rocketmq.integration.RocketMQInboundChannelAdapter;
import org.springframework.cloud.stream.binder.rocketmq.integration.RocketMQMessageHandler;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.springframework.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageChannelBinder extends
		AbstractMessageChannelBinder<ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>, RocketMQTopicProvisioner>
		implements
		ExtendedPropertiesBinder<MessageChannel, RocketMQConsumerProperties, RocketMQProducerProperties> {

	private static final Logger logger = LoggerFactory
			.getLogger(RocketMQMessageChannelBinder.class);

	private final RocketMQExtendedBindingProperties extendedBindingProperties;
	private final RocketMQTopicProvisioner rocketTopicProvisioner;
	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;
	private final InstrumentationManager instrumentationManager;
	private final ConsumersManager consumersManager;

	public RocketMQMessageChannelBinder(ConsumersManager consumersManager,
			RocketMQExtendedBindingProperties extendedBindingProperties,
			RocketMQTopicProvisioner provisioningProvider,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			InstrumentationManager instrumentationManager) {
		super(null, provisioningProvider);
		this.consumersManager = consumersManager;
		this.extendedBindingProperties = extendedBindingProperties;
		this.rocketTopicProvisioner = provisioningProvider;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketMQProducerProperties> producerProperties,
			MessageChannel errorChannel) throws Exception {
		if (producerProperties.getExtension().getEnabled()) {
			return new RocketMQMessageHandler(destination.getName(),
					producerProperties.getExtension(),
					rocketBinderConfigurationProperties, instrumentationManager);
		}
		else {
			throw new RuntimeException("Binding for channel " + destination.getName()
					+ "has been disabled, message can't be delivered");
		}
	}

	@Override
	protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
			String group,
			ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties)
			throws Exception {
		if (group == null || "".equals(group)) {
			throw new RuntimeException(
					"'group' must be configured for channel + " + destination.getName());
		}

		RocketMQInboundChannelAdapter rocketInboundChannelAdapter = new RocketMQInboundChannelAdapter(
				consumersManager, consumerProperties, destination.getName(), group,
				instrumentationManager);

		ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination,
				group, consumerProperties);
		if (consumerProperties.getMaxAttempts() > 1) {
			rocketInboundChannelAdapter
					.setRetryTemplate(buildRetryTemplate(consumerProperties));
			rocketInboundChannelAdapter
					.setRecoveryCallback(errorInfrastructure.getRecoverer());
		}
		else {
			rocketInboundChannelAdapter
					.setErrorChannel(errorInfrastructure.getErrorChannel());
		}

		return rocketInboundChannelAdapter;
	}

	@Override
	public RocketMQConsumerProperties getExtendedConsumerProperties(String channelName) {
		return extendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public RocketMQProducerProperties getExtendedProducerProperties(String channelName) {
		return extendedBindingProperties.getExtendedProducerProperties(channelName);
	}
}
