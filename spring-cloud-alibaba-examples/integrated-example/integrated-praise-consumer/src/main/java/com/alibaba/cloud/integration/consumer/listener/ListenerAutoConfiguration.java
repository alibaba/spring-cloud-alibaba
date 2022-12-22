package com.alibaba.cloud.integration.consumer.listener;

import java.util.function.Consumer;

import com.alibaba.cloud.integration.consumer.message.PraiseMessage;
import com.alibaba.cloud.integration.consumer.service.PraiseService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
public class ListenerAutoConfiguration {
	@Bean
	public Consumer<Message<PraiseMessage>> consumer(PraiseService praiseService) {
		return msg -> {
			praiseService.praiseItem(msg.getPayload().getItemId());
		};
	}
}
