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

package com.alibaba.cloud.ai.tongyi.chat;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

class TongYiChatOptionsTests {

	@Test
	public void testChatOptions() {

		Generation mockClient = Mockito.mock(Generation.class);
		Constants.apiKey = "test";

		// Test start.

		var tongYiChatClient = new TongYiChatModel(mockClient,
				TongYiChatOptions.builder().withModel(Generation.Models.QWEN_TURBO).withTemperature(88.8).build());

		var tongYiChatParams = tongYiChatClient.toTongYiChatParams(new Prompt("你好"));

		assertThat(tongYiChatParams.getMessages()).hasSize(1);

		assertThat(tongYiChatParams.getModel()).isEqualTo(Generation.Models.QWEN_TURBO);
		assertThat(tongYiChatParams.getTemperature()).isEqualTo(88.8f);

		tongYiChatClient = new TongYiChatModel(mockClient,
				TongYiChatOptions.builder().withModel(Generation.Models.QWEN_MAX).withTemperature(77.7).build());

		tongYiChatParams = tongYiChatClient.toTongYiChatParams(new Prompt("你是谁"));

		assertThat(tongYiChatParams.getMessages()).hasSize(1);

		assertThat(tongYiChatParams.getModel()).isEqualTo(Generation.Models.QWEN_MAX);
		assertThat(tongYiChatParams.getTemperature()).isEqualTo(77.7f);

		// Test end.
	}

}
