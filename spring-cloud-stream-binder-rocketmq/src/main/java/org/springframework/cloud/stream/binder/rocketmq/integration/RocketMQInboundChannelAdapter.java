package org.springframework.cloud.stream.binder.rocketmq.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.consuming.CloudStreamMessageListener;
import org.springframework.cloud.stream.binder.rocketmq.consuming.CloudStreamMessageListenerConcurrently;
import org.springframework.cloud.stream.binder.rocketmq.consuming.CloudStreamMessageListenerOrderly;
import org.springframework.cloud.stream.binder.rocketmq.consuming.ConsumersManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.ConsumerInstrumentation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQInboundChannelAdapter extends MessageProducerSupport {

    private ConsumerInstrumentation consumerInstrumentation;

    private final ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties;

    private final String destination;

    private final String group;

    private final InstrumentationManager instrumentationManager;

    private final ConsumersManager consumersManager;

    private RetryTemplate retryTemplate;

    private RecoveryCallback<? extends Object> recoveryCallback;

    public RocketMQInboundChannelAdapter(ConsumersManager consumersManager,
                                         ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties,
                                         String destination, String group,
                                         InstrumentationManager instrumentationManager) {
        this.consumersManager = consumersManager;
        this.consumerProperties = consumerProperties;
        this.destination = destination;
        this.group = group;
        this.instrumentationManager = instrumentationManager;
    }

    @Override
    protected void doStart() {
        if (!consumerProperties.getExtension().getEnabled()) {
            return;
        }

        String tags = consumerProperties == null ? null : consumerProperties.getExtension().getTags();
        Boolean isOrderly = consumerProperties == null ? false : consumerProperties.getExtension().getOrderly();

        DefaultMQPushConsumer consumer = consumersManager.getOrCreateConsumer(group, destination, consumerProperties);

        final CloudStreamMessageListener listener = isOrderly ? new CloudStreamMessageListenerOrderly(
            instrumentationManager, msg -> sendMessage(msg))
            : new CloudStreamMessageListenerConcurrently(instrumentationManager, msg -> sendMessage(msg));
        listener.setTopic(destination);

        Set<String> tagsSet = tags == null ? new HashSet<>() : Arrays.stream(tags.split("\\|\\|")).map(String::trim).collect(
            Collectors.toSet());

        consumerInstrumentation = instrumentationManager.getConsumerInstrumentation(destination);
        instrumentationManager.addHealthInstrumentation(consumerInstrumentation);

        try {
            if (!StringUtils.isEmpty(consumerProperties.getExtension().getSql())) {
                consumer.subscribe(destination, MessageSelector.bySql(consumerProperties.getExtension().getSql()));
            } else {
                consumer.subscribe(destination, String.join(" || ", tagsSet));
            }
            consumerInstrumentation.markStartedSuccessfully();
        } catch (MQClientException e) {
            consumerInstrumentation.markStartFailed(e);
            logger.error("RocketMQ Consumer hasn't been subscribed. Caused by " + e.getErrorMessage(), e);
            throw new RuntimeException("RocketMQ Consumer hasn't been subscribed.", e);
        }

        consumer.registerMessageListener(listener);

        try {
            consumersManager.startConsumer(group);
        } catch (MQClientException e) {
            logger.error("RocketMQ Consumer startup failed. Caused by " + e.getErrorMessage(), e);
            throw new RuntimeException("RocketMQ Consumer startup failed.", e);
        }
    }

    @Override
    protected void doStop() {
        consumersManager.stopConsumer(group);
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public void setRecoveryCallback(RecoveryCallback<? extends Object> recoveryCallback) {
        this.recoveryCallback = recoveryCallback;
    }
}
