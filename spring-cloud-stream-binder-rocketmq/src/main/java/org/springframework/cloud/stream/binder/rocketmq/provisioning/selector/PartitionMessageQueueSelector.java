package org.springframework.cloud.stream.binder.rocketmq.provisioning.selector;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.BinderHeaders;

import java.util.List;

/**
 * @author wangxing
 * @create 2019/7/3
 */
public class PartitionMessageQueueSelector implements MessageQueueSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionMessageQueueSelector.class);

    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        Integer partition = 0;
        try {
            partition = Integer.valueOf(msg.getProperty(BinderHeaders.PARTITION_HEADER));
        } catch (NumberFormatException ignored) {
        }
        if (partition >= mqs.size()) {
            LOGGER.warn("the partition '{}' is greater than the number of queues '{}'.", partition, mqs.size());
            partition = partition % mqs.size();
        }
        return mqs.get(partition);
    }

}