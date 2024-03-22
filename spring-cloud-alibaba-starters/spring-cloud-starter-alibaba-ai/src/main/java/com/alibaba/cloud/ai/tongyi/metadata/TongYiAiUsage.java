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

package com.alibaba.cloud.ai.tongyi.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

/**
 * {@link Usage} implementation for {@literal Alibaba DashScope}.
 *
 * @author yuluo
 * @since 2023.0.0.0
 */

public class TongYiAiUsage implements Usage {

	private final GenerationUsage usage;

	public TongYiAiUsage(GenerationUsage usage) {

		Assert.notNull(usage, "GenerationUsage must not be null");
		this.usage = usage;
	}

	public static TongYiAiUsage from(GenerationResult chatCompletions) {

		Assert.notNull(chatCompletions, "ChatCompletions must not be null");
		return from(chatCompletions.getUsage());
	}

	public static TongYiAiUsage from(GenerationUsage usage) {

		return new TongYiAiUsage(usage);
	}

	protected GenerationUsage getUsage() {

		return this.usage;
	}

	@Override
	public Long getPromptTokens() {
		return null;
	}

	@Override
	public Long getGenerationTokens() {
		return this.getUsage().getOutputTokens().longValue();
	}

	@Override
	public Long getTotalTokens() {
		return this.getUsage().getInputTokens().longValue() + this.getUsage().getInputTokens().longValue();
	}

	@Override
	public String toString() {
		return this.getUsage().toString();
	}
}
