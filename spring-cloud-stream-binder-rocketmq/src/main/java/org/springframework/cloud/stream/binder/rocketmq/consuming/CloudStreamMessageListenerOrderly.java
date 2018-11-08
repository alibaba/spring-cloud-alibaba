package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.List;
import java.util.function.Consumer;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.messaging.Message;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class CloudStreamMessageListenerOrderly extends CloudStreamMessageListener implements MessageListenerOrderly {

    public CloudStreamMessageListenerOrderly(InstrumentationManager instrumentationManager,
                                             Consumer<Message> sendMsgAction) {
        super(instrumentationManager, sendMsgAction);
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        Acknowledgement acknowledgement = consumeMessage(msgs);
        context.setSuspendCurrentQueueTimeMillis((acknowledgement.getConsumeOrderlySuspendCurrentQueueTimeMill()));
        return acknowledgement.getConsumeOrderlyStatus();
    }

}
