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
