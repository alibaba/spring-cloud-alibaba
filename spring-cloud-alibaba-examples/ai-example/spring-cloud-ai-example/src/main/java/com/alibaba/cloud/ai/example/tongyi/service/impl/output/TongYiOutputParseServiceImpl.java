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

package com.alibaba.cloud.ai.example.tongyi.service.impl.output;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.models.ActorsFilms;
import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * The BeanOutputParser generates an TongYI JSON compliant schema for a
 * JavaBean and provides instructions to use that schema when replying to a request.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0
 */

@Service
public class TongYiOutputParseServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiOutputParseServiceImpl.class);

	private final ChatModel chatModel;

	public TongYiOutputParseServiceImpl(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public ActorsFilms genOutputParse(String actor) {

		var outputParser = new BeanOutputConverter<>(ActorsFilms.class);

		String format = outputParser.getFormat();
		logger.info("format: " + format);
		String userMessage = """
				Generate the filmography for the actor {actor}.
				{format}
				""";
		PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actor, "format", format));
		Prompt prompt = promptTemplate.create();
		Generation generation = chatModel.call(prompt).getResult();

		// {@link BeanOutputParser#getFormat}
		// simple solve.
		String content = generation.getOutput().getContent()
				.replace("```json", "")
				.replace("```", "");

		return outputParser.parse(content);
	}
}
