package com.alibaba.cloud.ai.tongyi.client;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiChatClient implements ChatClient, StreamingChatClient {

	private static final Logger logger = LoggerFactory.getLogger(TongYiChatClient.class);

	@Override
	public ChatResponse call(Prompt prompt) {

		ArrayList<Generation> generations = new ArrayList<>();
		generations.add(new Generation("test"));

		return new ChatResponse(generations);
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {

		return null;
	}
}
