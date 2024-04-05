package com.alibaba.cloud.ai.tongyi.metadata;

import java.nio.charset.StandardCharsets;

import com.alibaba.cloud.ai.tongyi.MockTongYiAITestConfiguration;
import com.alibaba.cloud.ai.tongyi.TongYiChatClient;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@SpringBootTest
@ActiveProfiles("spring-ai-tongyi-ai-mocks")
@ContextConfiguration(classes = TongYiChatClientMetadataTests.TestConfiguration.class)
@SuppressWarnings("unused")
class TongYiChatClientMetadataTests {

	@Autowired
	private TongYiChatClient chatClient;

	@Test
	void tongYiAIChatMetadataCapturedDuringGeneration() {

		Prompt prompt = new Prompt("Tell me a laugh");
		Constants.apiKey="sk-a3d73b1709bf4a178c28ed7c8b3b5a45";

		ChatResponse response = this.chatClient.call(prompt);

		assertThat(response).isNotNull();

		Generation generation = response.getResult();

		assertThat(generation).isNotNull()
				.extracting(Generation::getOutput)
				.extracting(AssistantMessage::getContent)
				.isEqualTo("Sure! Here's a classic joke that always gets a chuckle:\\n\\nWhy don't scientists trust atoms?\\n\\nBecause they make up everything!");

		assertPromptMetadata(response);
		assertGenerationMetadata(response);
		assertChoiceMetadata(generation);
	}

	private void assertPromptMetadata(ChatResponse response) {

		PromptMetadata promptMetadata = response.getMetadata().getPromptMetadata();

		assertThat(promptMetadata).isNotNull();

		PromptMetadata.PromptFilterMetadata promptFilterMetadata = promptMetadata.findByPromptIndex(0).orElse(null);

		assertThat(promptFilterMetadata).isNotNull();
		assertThat(promptFilterMetadata.getPromptIndex()).isZero();
	}

	private void assertGenerationMetadata(ChatResponse response) {

		ChatResponseMetadata chatResponseMetadata = response.getMetadata();

		assertThat(chatResponseMetadata).isNotNull();
		assertThat(chatResponseMetadata.getRateLimit().getRequestsLimit())
				.isEqualTo(new EmptyRateLimit().getRequestsLimit());

		Usage usage = chatResponseMetadata.getUsage();

		assertThat(usage).isNotNull();
		assertThat(usage.getPromptTokens()).isEqualTo(58);
		assertThat(usage.getGenerationTokens()).isEqualTo(68);
		assertThat(usage.getTotalTokens()).isEqualTo(126);
	}

	private void assertChoiceMetadata(Generation generation) {

		ChatGenerationMetadata chatGenerationMetadata = generation.getMetadata();

		assertThat(chatGenerationMetadata).isNotNull();
		assertThat(chatGenerationMetadata.getFinishReason()).isEqualTo("stop");
	}


	@SpringBootConfiguration
	@Profile("spring-ai-tongyi-ai-mocks")
	@Import(MockTongYiAITestConfiguration.class)
	static class TestConfiguration {

		@Bean
		MockMvc mockMvc() {

			return MockMvcBuilders.standaloneSetup(new SCATongYiAIChatCompletionsController()).build();
		}

	}

	@RestController
	@RequestMapping("/spring-cloud-alibaba-ai/api")
	@SuppressWarnings("all")
	static class SCATongYiAIChatCompletionsController {

		@PostMapping("/api/v1/services/aigc/text-generation/generation")
		ResponseEntity<?> chatCompletions(WebRequest request) {

			String json = getJson();

			ResponseEntity<?> response = ResponseEntity.status(HttpStatusCode.valueOf(200))
					.contentType(MediaType.APPLICATION_JSON)
					.contentLength(json.getBytes(StandardCharsets.UTF_8).length)
					.body(json);

			return response;
		}

		private String getJson() {
			return """
					{
						"output": {
							"choices": [{
								"finish_reason": "stop",
								"message": {
									"role": "assistant",
									"content": "Sure! Here's a classic joke that always gets a chuckle:\\n\\nWhy don't scientists trust atoms?\\n\\nBecause they make up everything!"
								}
							}]
						},
						"usage": {
							"total_tokens": 50,
							"output_tokens": 27,
							"input_tokens": 23
						},
						"request_id": "fc8ae47b-603d-95e7-80e9-0a10e6427256"
					}
					""";
		}

	}

}
