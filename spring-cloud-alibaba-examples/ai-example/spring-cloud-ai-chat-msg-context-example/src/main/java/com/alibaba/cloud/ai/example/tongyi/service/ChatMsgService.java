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

package com.alibaba.cloud.ai.example.tongyi.service;

import com.alibaba.cloud.ai.example.tongyi.context.MessageContextHolder;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class ChatMsgService {

	private final ChatModel chatModel;

	private final MessageContextHolder messageContextHolder;

	@Autowired
	public ChatMsgService(ChatModel chatModel, MessageContextHolder messageContextHolder) {
		this.chatModel = chatModel;
		this.messageContextHolder = messageContextHolder;
	}

	public String completion(String message) {

		// create chat prompt
		Prompt prompt = new Prompt(new UserMessage(message));

		// collect user message
		messageContextHolder.addMsg(
				messageContextHolder.getSCASessionId(),
				prompt.getInstructions().get(0)
		);

		ChatResponse resp = chatModel.call(prompt);

		// collect model response
		messageContextHolder.addMsg(
				messageContextHolder.getSCASessionId(),
				resp.getResult().getOutput()
		);

		return resp.getResult().getOutput().getContent();

	}

}
