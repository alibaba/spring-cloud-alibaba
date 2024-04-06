package com.alibaba.cloud.ai.example.tongyi.service.impl.prompttemplate;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * The TongYiPromptTemplateServiceImpl shows how to use the StringTemplate Engine and the Spring AI PromptTemplate class.
 * In the resources\prompts directory is the file joke-prompt.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@Slf4j
@Service
public class TongYiPromptTemplateServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final ChatClient chatClient;

	@Value("classpath:/prompts/joke-prompt.st")
	private Resource jokeResource;

	public TongYiPromptTemplateServiceImpl(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public AssistantMessage genPromptTemplates(String adjective, String topic) {

		PromptTemplate promptTemplate = new PromptTemplate(jokeResource);

		Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));
		return chatClient.call(prompt).getResult().getOutput();
	}
}
