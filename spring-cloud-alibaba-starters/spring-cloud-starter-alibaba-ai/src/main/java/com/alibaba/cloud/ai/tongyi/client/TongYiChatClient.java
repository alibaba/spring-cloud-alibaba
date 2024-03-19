package com.alibaba.cloud.ai.tongyi.client;

import java.util.List;
import java.util.stream.StreamSupport;

import com.alibaba.cloud.ai.tongyi.TongYiChatOptions;
import com.alibaba.cloud.ai.tongyi.exception.TongYiException;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
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

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiChatClient implements ChatClient, StreamingChatClient {

	private static final Logger logger = LoggerFactory.getLogger(TongYiChatClient.class);

	private final Generation generation;

	private TongYiChatOptions defaultOptions;

	public TongYiChatClient(Generation generation) {

		this(generation,
				TongYiChatOptions.builder()
						.withTopP(0.8)
						.withEnableSearch(true)
						.withResultFormat(QwenParam.ResultFormat.MESSAGE)
						.build()
		);
	}

	public TongYiChatClient(Generation generation, TongYiChatOptions options) {

		this.generation = generation;
		this.defaultOptions = options;
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		GenerationResult res = null;
		try {
			res = generation.call(toTongYiChatParams(prompt));
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

		QwenParam params = toTongYiChatParams(prompt);
		Flowable<GenerationResult> generationResultFlowable = null;

		try {
			generationResultFlowable = generation.streamCall(params);
		}
		catch (NoApiKeyException | InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}

		return Flux.fromStream(
				StreamSupport
						.stream(
								Flowable
										.fromPublisher(generationResultFlowable)
										.blockingIterable()
										.spliterator(),
								false
						)
						.flatMap(
								res -> res.getOutput()
										.getChoices()
										.stream()
										.map(choice -> {
													var content = (choice.getMessage() != null) ? choice.getMessage()
															.getContent() : null;
													var generation1 = new org.springframework.ai.chat.Generation(content);
													return new ChatResponse(List.of(generation1));
												}
										)
						)
		).publishOn(Schedulers.parallel());

	}

	private QwenParam toTongYiChatParams(Prompt prompt) {

		Constants.apiKey = this.defaultOptions.getApiKey();

		System.out.println(this.defaultOptions.toString());

		return QwenParam.builder()
				.model(this.defaultOptions.getModel())
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

}
