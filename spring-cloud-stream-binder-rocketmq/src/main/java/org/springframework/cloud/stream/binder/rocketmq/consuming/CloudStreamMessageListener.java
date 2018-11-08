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

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public abstract class CloudStreamMessageListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentationManager instrumentationManager;

    private final Consumer<Message> sendMsgAction;

    private String topic;

    CloudStreamMessageListener(InstrumentationManager instrumentationManager, Consumer<Message> sendMsgAction) {
        this.instrumentationManager = instrumentationManager;
        this.sendMsgAction = sendMsgAction;
    }

    Acknowledgement consumeMessage(final List<MessageExt> msgs) {
        List<Acknowledgement> acknowledgements = new ArrayList<>();
        msgs.forEach(msg -> {
            logger.info("consuming msg:\n" + msg);
            logger.debug("message body:\n" + new String(msg.getBody()));
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
                instrumentationManager.getConsumerInstrumentation(getTopic())
                    .markConsumed();
            } catch (Exception e) {
                logger.error("RocketMQ Message hasn't been processed successfully. Caused by ", e);
                instrumentationManager.getConsumerInstrumentation(getTopic())
                    .markConsumedFailure();
                throw e;
            }
        });
        return acknowledgements.get(0);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
