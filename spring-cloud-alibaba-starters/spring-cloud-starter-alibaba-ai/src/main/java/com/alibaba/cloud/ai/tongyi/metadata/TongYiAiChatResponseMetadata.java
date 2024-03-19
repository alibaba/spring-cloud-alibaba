package com.alibaba.cloud.ai.tongyi.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiAiChatResponseMetadata implements ChatResponseMetadata {

	protected static final String AI_METADATA_STRING = "{ @type: %1$s, id: %2$s, usage: %3$s, rateLimit: %4$s }";

	@SuppressWarnings("all")
	public static TongYiAiChatResponseMetadata from(GenerationResult chatCompletions,
			PromptMetadata promptFilterMetadata) {

		Assert.notNull(chatCompletions, "Alibaba ai ChatCompletions must not be null");
		String id = chatCompletions.getRequestId();
		TongYiAiUsage usage = TongYiAiUsage.from(chatCompletions);

		return new TongYiAiChatResponseMetadata(
				id,
				usage,
				promptFilterMetadata
		);
	}

	private final String id;

	private final Usage usage;

	private final PromptMetadata promptMetadata;

	protected TongYiAiChatResponseMetadata(String id, TongYiAiUsage usage, PromptMetadata promptMetadata) {

		this.id = id;
		this.usage = usage;
		this.promptMetadata = promptMetadata;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public Usage getUsage() {
		return this.usage;
	}

	@Override
	public PromptMetadata getPromptMetadata() {
		return this.promptMetadata;
	}

	@Override
	public String toString() {

		return AI_METADATA_STRING.formatted(getClass().getTypeName(), getId(), getUsage(), getRateLimit());
	}

}
