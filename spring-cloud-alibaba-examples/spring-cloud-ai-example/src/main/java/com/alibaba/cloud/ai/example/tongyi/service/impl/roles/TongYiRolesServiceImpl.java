package com.alibaba.cloud.ai.example.tongyi.service.impl.roles;

import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@Slf4j
@Service
public class TongYiRolesServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final ChatClient chatClient;

	public TongYiRolesServiceImpl(ChatClient chatClient) {
		this.chatClient = chatClient;
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

		return chatClient.call(prompt).getResult().getOutput();
	}
}
