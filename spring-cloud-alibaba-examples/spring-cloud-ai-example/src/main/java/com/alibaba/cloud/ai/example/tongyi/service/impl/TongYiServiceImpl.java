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

package com.alibaba.cloud.ai.example.tongyi.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.models.ActorsFilms;
import com.alibaba.cloud.ai.example.tongyi.models.Completion;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @since 2023.0.0.0
 */

@Slf4j
@Service
public class TongYiServiceImpl implements TongYiService {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	@Autowired
	private MessageManager msgManager;

	@Value("classpath:/prompts/joke-prompt.st")
	private Resource jokeResource;

	@Value("classpath:/prompts/assistant-message.st")
	private Resource systemResource;

	@Value("classpath:/docs/wikipedia-curling.md")
	private Resource docsToStuffResource;

	@Value("classpath:/prompts/qa-prompt.st")
	private Resource qaPromptResource;

	private final ChatClient chatClient;

	private final StreamingChatClient streamingChatClient;

	@Autowired
	public TongYiServiceImpl(ChatClient chatClient, StreamingChatClient streamingChatClient) {

		this.chatClient = chatClient;
		this.streamingChatClient = streamingChatClient;
	}

	@Override
	public String completion(String message) {

		Message userMsg = Message.builder()
				.role(Role.USER.getValue())
				.content(message)
				.build();
		msgManager.add(userMsg);

		return chatClient.call(message);
	}

	@Override
	public Map<String, String> streamCompletion(String message) {

		StringBuilder fullContent = new StringBuilder();

		streamingChatClient.stream(new Prompt(message))
				.flatMap(chatResponse -> Flux.fromIterable(chatResponse.getResults()))
				.map(content -> content.getOutput().getContent())
				.doOnNext(fullContent::append)
				.last()
				.map(lastContent -> Map.of(message, fullContent.toString()))
				.block();

		log.info(fullContent.toString());

		return Map.of(message, fullContent.toString());
	}

	@Override
	public ActorsFilms genOutputParse(String actor) {

		var outputParser = new BeanOutputParser<>(ActorsFilms.class);

		String format = outputParser.getFormat();
		logger.info("format: " + format);
		String userMessage = """
				Generate the filmography for the actor {actor}.
				{format}
				""";
		PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actor, "format", format));
		Prompt prompt = promptTemplate.create();
		Generation generation = chatClient.call(prompt).getResult();

		// {@link BeanOutputParser#getFormat}
		// simple solve.
		String content = generation.getOutput().getContent()
				.replace("```json", "")
				.replace("```", "");

		return outputParser.parse(content);
	}

	@Override
	public AssistantMessage genPromptTemplates(String adjective, String topic) {

		PromptTemplate promptTemplate = new PromptTemplate(jokeResource);

		Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));
		return chatClient.call(prompt).getResult().getOutput();
	}

	// todo
	@Override
	public AssistantMessage genRole(String message, String name, String voice) {

		/**
		TongYi model rules: Role must be user or assistant and Content length must be greater than 0.
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
		org.springframework.ai.chat.messages.Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
		*/

		UserMessage userMessage = new UserMessage(message);
		AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(systemResource);
		org.springframework.ai.chat.messages.Message assistantPromptTemplateMessage = assistantPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
		Prompt prompt = new Prompt(List.of(userMessage, assistantPromptTemplateMessage));

		return chatClient.call(prompt).getResult().getOutput();
	}

	// TongYi model: Range of input length should be [1, 6000]
	@Override
	public Completion stuffCompletion(String message, boolean stuffit) {

		PromptTemplate promptTemplate = new PromptTemplate(qaPromptResource);
		Map<String, Object> map = new HashMap<>();
		map.put("question", message);

		if (stuffit) {
			map.put("context", docsToStuffResource);
		}
		else {
			map.put("context", "");
		}

		Prompt prompt = promptTemplate.create(map);
		Generation generation = chatClient.call(prompt).getResult();
		return new Completion(generation.getOutput().getContent());
	}
}
