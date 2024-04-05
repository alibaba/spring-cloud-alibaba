package com.alibaba.cloud.ai.tongyi;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yuluo
 */


public class TongYiChatOptionsTests {

	@Test
	public void createRequestWithChatOptions() {

		Generation mockClient = Mockito.mock(Generation.class);
		Constants.apiKey="test";

		// Test start.

		var tongYiChatClient = new TongYiChatClient(mockClient,
				TongYiChatOptions.builder().withModel(Generation.Models.QWEN_TURBO).withTemperature(88.8).build());

		var tongYiChatParams = tongYiChatClient.toTongYiChatParams(new Prompt("This is a test message"));

		assertThat(tongYiChatParams.getMessages()).hasSize(1);

		assertThat(tongYiChatParams.getModel()).isEqualTo(Generation.Models.QWEN_TURBO);
		assertThat(tongYiChatParams.getTemperature()).isEqualTo(88.8f);

		tongYiChatClient = new TongYiChatClient(mockClient,
				TongYiChatOptions.builder().withModel(Generation.Models.QWEN_MAX).withTemperature(77.7).build());

		tongYiChatParams = tongYiChatClient.toTongYiChatParams(new Prompt("This is a test message"));

		assertThat(tongYiChatParams.getMessages()).hasSize(1);

		assertThat(tongYiChatParams.getModel()).isEqualTo(Generation.Models.QWEN_MAX);
		assertThat(tongYiChatParams.getTemperature()).isEqualTo(77.7f);

		// Test end.
	}

}
