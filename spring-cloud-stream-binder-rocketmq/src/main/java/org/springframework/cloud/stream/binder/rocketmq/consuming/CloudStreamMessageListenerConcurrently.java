package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.List;
import java.util.function.Consumer;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.messaging.Message;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class CloudStreamMessageListenerConcurrently extends CloudStreamMessageListener implements
    MessageListenerConcurrently {

    public CloudStreamMessageListenerConcurrently(InstrumentationManager instrumentationManager,
                                                  Consumer<Message> sendMsgAction) {
        super(instrumentationManager, sendMsgAction);
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
        Acknowledgement acknowledgement = consumeMessage(msgs);
        context.setDelayLevelWhenNextConsume(acknowledgement.getConsumeConcurrentlyDelayLevel());
        return acknowledgement.getConsumeConcurrentlyStatus();
    }

}