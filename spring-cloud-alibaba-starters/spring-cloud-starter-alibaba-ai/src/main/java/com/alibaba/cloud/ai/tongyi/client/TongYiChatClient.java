/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.tongyi.client;

import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.ai.tongyi.TongYiChatOptions;
import com.alibaba.cloud.ai.tongyi.exception.TongYiException;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * {@link ChatClient} and {@link StreamingChatClient} implementation for {@literal Alibaba DashScope}
 * backed by {@link Generation}.
 * @author yuluo
 * @since 2023.0.0.0
 */

public class TongYiChatClient implements ChatClient, StreamingChatClient {

	private static final Logger logger = LoggerFactory.getLogger(TongYiChatClient.class);

	/**
	 * DashScope generation client.
	 */
	private final Generation generation;

	/**
	 * The TongYi models default chat completion api.
	 */
	private TongYiChatOptions defaultOptions;

	/**
	 * User role message manager.
	 */
	@Autowired
	private MessageManager msgManager;

	/**
	 * Initializes an instance of the TongYiChatClient.
	 * @param generation DashScope generation client.
	 */
	public TongYiChatClient(Generation generation) {

		this(generation,
				TongYiChatOptions.builder()
						.withTopP(0.8)
						.withEnableSearch(true)
						.withResultFormat(QwenParam.ResultFormat.MESSAGE)
						.build()
		);
	}

	/**
	 * Create a TongYi models client.
	 * @param generation DashScope model generation client.
	 * @param options TongYi default chat completion api.
	 */
	public TongYiChatClient(Generation generation, TongYiChatOptions options) {

		this.generation = generation;
		this.defaultOptions = options;
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		GenerationResult res = null;

		try {
			res = generation.call(toTongYiChatParams(prompt));
			msgManager.add(res);
		}
		catch (NoApiKeyException | InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}

		List<org.springframework.ai.chat.Generation> generations =
				res
						.getOutput()
						.getChoices()
						.stream()
						.map(choice ->
								new org.springframework.ai.chat.Generation(
										choice
												.getMessage()
												.getContent()
								).withGenerationMetadata(generateChoiceMetadata(choice)
								))
						.toList();

		return new ChatResponse(generations);

	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {

		Flowable<GenerationResult> genRes = null;

		try {
			genRes = generation.streamCall(toTongYiChatParams(prompt));
		}
		catch (NoApiKeyException | InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}

		return Flux.from(genRes)
				.flatMap(
						message -> Flux.just(
								message.getOutput()
										.getChoices()
										.get(0)
										.getMessage()
										.getContent())
								.map(content -> {
									var gen = new org.springframework.ai.chat.Generation(content)
											.withGenerationMetadata(generateChoiceMetadata(
													message.getOutput()
															.getChoices()
															.get(0)
												));
									return new ChatResponse(List.of(gen));
								})
				)
				.publishOn(Schedulers.parallel());

	}

	/**
	 * Configuration properties to Qwen model params.
	 * @param prompt {@link Prompt}
	 * @return Qwen models params {@link QwenParam}
	 */
	private QwenParam toTongYiChatParams(Prompt prompt) {

		Constants.apiKey = getKey();

		return QwenParam.builder()
				.model(this.defaultOptions.getModel())
				.messages(msgManager.get())
				.resultFormat(this.defaultOptions.getResultFormat())
				.topP(this.defaultOptions.getTopP().doubleValue())
				.topK(this.defaultOptions.getTopK())
				.enableSearch(this.defaultOptions.getEnableSearch())
				.seed(this.defaultOptions.getSeed())
				.maxTokens(this.defaultOptions.getMaxTokens())
				.repetitionPenalty(this.defaultOptions.getRepetitionPenalty())
				.temperature(this.defaultOptions.getTemperature())
				.incrementalOutput(this.defaultOptions.getIncrementalOutput())
				.prompt(prompt.getContents())
				.build();
	}

	private ChatGenerationMetadata generateChoiceMetadata(GenerationOutput.Choice choice) {

		return ChatGenerationMetadata.from(
				String.valueOf(choice.getFinishReason()),
				choice.getMessage().getContent()
		);
	}

	/**
	 * Get TongYi model api_key .
	 * todo: Get key from env and env_file.
	 * @return api_key.
	 */
	private String getKey() {

		String apiKey = null;

		if (Objects.nonNull(this.defaultOptions.getApiKey())) {
			apiKey = this.defaultOptions.getApiKey();
		}
		return apiKey;
	}

}
