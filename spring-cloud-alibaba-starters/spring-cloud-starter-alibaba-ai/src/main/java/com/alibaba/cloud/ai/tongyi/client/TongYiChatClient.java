package com.alibaba.cloud.ai.tongyi.client;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.ai.tongyi.exception.TongYiException;
import com.alibaba.cloud.ai.tongyi.properties.TongYiProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiChatClient implements ChatClient, StreamingChatClient {

	private static final Logger logger = LoggerFactory.getLogger(TongYiChatClient.class);

	private final Generation generation;

	@Resource
	private MessageManager messageManager;

	private QwenParam param;

	public TongYiChatClient(Generation generation) {

		this.generation = generation;
	}

	@Resource
	private TongYiProperties properties;

	/**
	 * Todo: SDK has three methods set api_key!
	 */
	@PostConstruct
	public void init() {

		String apiKey = properties.getApiKey();

		if (Objects.isNull(apiKey))  {
			throw new TongYiException("API_KEY must not be null");
		}

		Constants.apiKey = apiKey;
		param = QwenParam.builder()
				.model(properties.getModel())
				.messages(messageManager.get())
				.resultFormat(properties.getResultFormat())
				.topP(properties.getTopP().doubleValue())
				.topK(properties.getTopK())
				.enableSearch(properties.getEnableSearch())
				.seed(properties.getSeed())
				.maxTokens(properties.getMaxTokens())
				.repetitionPenalty(properties.getRepetitionPenalty())
				.temperature(properties.getTemperature())
				.incrementalOutput(properties.getIncrementalOutput())
				.build();
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		GenerationResult res = null;
		param.setPrompt(prompt.getContents());

		try {
			res = generation.call(param);
		}
		catch (NoApiKeyException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}
		catch (InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

		if (Objects.nonNull(res)) {
			messageManager.add(res);
		}

		List<org.springframework.ai.chat.Generation> generations = res.getOutput().getChoices().stream()
				.map(item -> {
					String content = item.getMessage().getContent();
					System.out.println(content);
					return new org.springframework.ai.chat.Generation(JsonUtils.toJson(content));
				})
				.collect(Collectors.toList());

		return new ChatResponse(generations);
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {

		return null;
	}

}
