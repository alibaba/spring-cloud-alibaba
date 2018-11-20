package org.springframework.cloud.stream.binder.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.cloud.stream.binder.rocketmq.consuming.Acknowledgement;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ACKNOWLEDGEMENT_KEY;
import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ORIGINAL_ROCKET_MESSAGE;
import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ROCKET_FLAG;
import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ROCKET_SEND_RESULT;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageHeaderAccessor extends MessageHeaderAccessor {

    public RocketMQMessageHeaderAccessor() {
        super();
    }

    public RocketMQMessageHeaderAccessor(Message<?> message) {
        super(message);
    }

    public Acknowledgement getAcknowledgement(Message message) {
        return message.getHeaders().get(ACKNOWLEDGEMENT_KEY, Acknowledgement.class);
    }

    public RocketMQMessageHeaderAccessor withAcknowledgment(Acknowledgement acknowledgment) {
        setHeader(ACKNOWLEDGEMENT_KEY, acknowledgment);
        return this;
    }

    public String getTags() {
        return (String)getMessageHeaders().getOrDefault(MessageConst.PROPERTY_TAGS, "");
    }

    public RocketMQMessageHeaderAccessor withTags(String tag) {
        setHeader(MessageConst.PROPERTY_TAGS, tag);
        return this;
    }

    public String getKeys() {
        return (String)getMessageHeaders().getOrDefault(MessageConst.PROPERTY_KEYS, "");
    }

    public RocketMQMessageHeaderAccessor withKeys(String keys) {
        setHeader(MessageConst.PROPERTY_KEYS, keys);
        return this;
    }

    public MessageExt getRocketMessage() {
        return getMessageHeaders().get(ORIGINAL_ROCKET_MESSAGE, MessageExt.class);
    }

    public RocketMQMessageHeaderAccessor withRocketMessage(MessageExt message) {
        setHeader(ORIGINAL_ROCKET_MESSAGE, message);
        return this;
    }

    public Integer getDelayTimeLevel() {
        return (Integer)getMessageHeaders().getOrDefault(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 0);
    }

    public RocketMQMessageHeaderAccessor withDelayTimeLevel(Integer delayTimeLevel) {
        setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, delayTimeLevel);
        return this;
    }

    public Integer getFlag() {
        return (Integer)getMessageHeaders().getOrDefault(ROCKET_FLAG, 0);
    }

    public RocketMQMessageHeaderAccessor withFlag(Integer delayTimeLevel) {
        setHeader(ROCKET_FLAG, delayTimeLevel);
        return this;
    }

    public SendResult getSendResult() {
        return getMessageHeaders().get(ROCKET_SEND_RESULT, SendResult.class);
    }

    public static void putSendResult(MutableMessage message, SendResult sendResult) {
        message.getHeaders().put(ROCKET_SEND_RESULT, sendResult);
    }

    public Map<String, String> getUserProperties() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : this.toMap().entrySet()) {
            if (entry.getValue() instanceof String && !MessageConst.STRING_HASH_SET.contains(entry.getKey()) && !entry
                .getKey().equals(MessageHeaders.CONTENT_TYPE)) {
                result.put(entry.getKey(), (String)entry.getValue());
            }
        }
        return result;
    }
}
