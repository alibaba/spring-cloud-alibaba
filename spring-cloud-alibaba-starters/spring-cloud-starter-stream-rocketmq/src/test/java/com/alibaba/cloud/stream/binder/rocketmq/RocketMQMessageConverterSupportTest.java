package com.alibaba.cloud.stream.binder.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.apache.rocketmq.common.message.MessageConst;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sorie
 */
public class RocketMQMessageConverterSupportTest {

	@Test
	public void convertMessage2MQBlankHeaderTest() {
		String destination = "test";
		Message message = MessageBuilder.withPayload("msg")
				.setHeader(MessageConst.PROPERTY_TAGS, "a")
				.setHeader("test", "")
				.build();
		org.apache.rocketmq.common.message.Message rkmqMsg =
				RocketMQMessageConverterSupport.convertMessage2MQ(destination, message);
		String testProp = rkmqMsg.getProperty("test");
		String tagProp = rkmqMsg.getProperty(MessageConst.PROPERTY_TAGS);
		assertThat(testProp).isNull();
		assertThat(tagProp).isEqualTo("a");
	}
}
