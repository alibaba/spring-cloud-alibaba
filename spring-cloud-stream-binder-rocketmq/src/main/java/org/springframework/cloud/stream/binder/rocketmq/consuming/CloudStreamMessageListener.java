package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageHeaderAccessor;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public abstract class CloudStreamMessageListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConsumerPropertiesWrapper consumerPropertiesWrapper;

    private final InstrumentationManager instrumentationManager;

    private final Consumer<Message> sendMsgAction;

    CloudStreamMessageListener(InstrumentationManager instrumentationManager, Consumer<Message> sendMsgAction) {
        this.instrumentationManager = instrumentationManager;
        this.sendMsgAction = sendMsgAction;
    }

    public String getTagsString() {
        return String.join(" || ", consumerPropertiesWrapper.getTagsSet());
    }

    public void setConsumerPropertiesWrapper(String group, String topic, String tags) {
        this.consumerPropertiesWrapper = new ConsumerPropertiesWrapper(group, topic, tags);
    }

    Acknowledgement consumeMessage(final List<MessageExt> msgs) {
        List<Acknowledgement> acknowledgements = new ArrayList<>();
        msgs.forEach(msg -> {
            logger.info("consuming msg:\n" + msg);
            logger.debug("message body:\n" + new String(msg.getBody()));
            if (consumerPropertiesWrapper != null && msg.getTopic().equals(consumerPropertiesWrapper.getTopic())) {
                if (StringUtils.isEmpty(consumerPropertiesWrapper.getTags()) || consumerPropertiesWrapper.getTagsSet()
                    .contains(msg.getTags())) {
                    try {
                        Acknowledgement acknowledgement = new Acknowledgement();
                        Message<byte[]> toChannel = MessageBuilder.withPayload(msg.getBody()).
                            setHeaders(new RocketMQMessageHeaderAccessor().
                                withAcknowledgment(acknowledgement).
                                withTags(msg.getTags()).
                                withKeys(msg.getKeys()).
                                withFlag(msg.getFlag()).
                                withRocketMessage(msg)
                            ).build();
                        acknowledgements.add(acknowledgement);
                        sendMsgAction.accept(toChannel);
                        instrumentationManager.getConsumerInstrumentation(consumerPropertiesWrapper.getTopic())
                            .markConsumed();
                    } catch (Exception e) {
                        logger.error("Rocket Message hasn't been processed successfully. Caused by ", e);
                        instrumentationManager.getConsumerInstrumentation(consumerPropertiesWrapper.getTopic())
                            .markConsumedFailure();
                        throw e;
                    }
                }
            }
        });
        return acknowledgements.get(0);
    }
}
