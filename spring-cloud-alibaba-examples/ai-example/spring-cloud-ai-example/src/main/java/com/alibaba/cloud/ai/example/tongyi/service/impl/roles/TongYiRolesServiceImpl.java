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

package com.alibaba.cloud.ai.example.tongyi.service.impl.roles;

import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0
 */

@Service
public class TongYiRolesServiceImpl extends AbstractTongYiServiceImpl {

	private final ChatModel chatModel;

	public TongYiRolesServiceImpl(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Value("classpath:/prompts/assistant-message.st")
	private Resource systemResource;

	@Override
	public AssistantMessage genRole(String message, String name, String voice) {

		/**
		 TongYi model rules: Role must be user or assistant and Content length must be greater than 0.
		 SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
		 org.springframework.ai.chat.messages.Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

		 In TongYi models, System role must appear at the top of the message and can only appear once.
		 https://help.aliyun.com/zh/dashscope/developer-reference/api-details?spm=a2c4g.11186623.0.0.4dbcc11akAaRbs#b9ad0a10cfhpe
		 */

		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
		org.springframework.ai.chat.messages.Message systemPromptTemplateMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
		UserMessage userMessage = new UserMessage(message);

		Prompt prompt = new Prompt(List.of(systemPromptTemplateMessage, userMessage));

		return chatModel.call(prompt).getResult().getOutput();
	}
}
