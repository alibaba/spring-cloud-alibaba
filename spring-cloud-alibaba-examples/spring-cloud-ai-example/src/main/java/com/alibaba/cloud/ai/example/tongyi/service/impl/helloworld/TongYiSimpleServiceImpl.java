package com.alibaba.cloud.ai.example.tongyi.service.impl.helloworld;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple example.
 * There is optional message parameter whose default value is "Tell me a joke". pl The response to the request is from the TongYi models Service.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@Slf4j
@Service
public class TongYiSimpleServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final ChatClient chatClient;

	private final StreamingChatClient streamingChatClient;

	@Autowired
	public TongYiSimpleServiceImpl(ChatClient chatClient, StreamingChatClient streamingChatClient) {

		this.chatClient = chatClient;
		this.streamingChatClient = streamingChatClient;
	}

	@Override
	public String completion(String message) {

		Prompt prompt = new Prompt(new UserMessage(message));

		return chatClient.call(prompt).getResult().getOutput().getContent();
	}

	@Override
	public Map<String, String> streamCompletion(String message) {

		StringBuilder fullContent = new StringBuilder();

		streamingChatClient.stream(new Prompt(message))
				.flatMap(chatResponse -> Flux.fromIterable(chatResponse.getResults()))
				.map(content -> content.getOutput().getContent())
				.doOnNext(fullContent::append)
				.last()
				.map(lastContent -> Map.of(message, fullContent.toString()))
				.block();

		log.info(fullContent.toString());

		return Map.of(message, fullContent.toString());
	}

}
