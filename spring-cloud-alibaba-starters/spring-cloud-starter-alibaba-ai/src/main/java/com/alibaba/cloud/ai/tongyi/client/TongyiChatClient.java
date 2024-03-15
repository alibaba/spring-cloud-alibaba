package com.alibaba.cloud.ai.tongyi.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongyiChatClient extends
		AbstractFunctionCallSupport<
				ChatCompletionMessage,
				TongyiAPI.ChatCompletionRequest,
				ResponseEntity<ChatCompletion>>
		implements ChatClient, StreamingChatClient {

	private static final Logger logger = LoggerFactory.getLogger(TongyiChatClient.class);

	@Override
	public ChatResponse call(Prompt prompt) {
		return null;
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		return null;
	}

}
