/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.ai.rag.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class RAGService {

	@Value("classpath:/prompts/system-qa.st")
	private Resource systemBeerPrompt;

	@Value("${topk:10}")
	private int topK;

	private final ChatModel chatModel;

	private final VectorStore store;

	public RAGService(ChatModel chatModel, VectorStore store) {

		this.chatModel = chatModel;
		this.store = store;
	}

	public Generation retrieve(String message) {

		SearchRequest request = SearchRequest.query(message).withTopK(topK);
		List<Document> docs = store.similaritySearch(request);

		Message systemMessage = getSystemMessage(docs);
		UserMessage userMessage = new UserMessage(message);

		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
		ChatResponse response = chatModel.call(prompt);

		return response.getResult();
	}

	private Message getSystemMessage(List<Document> similarDocuments) {

		String documents = similarDocuments.stream()
				.map(Document::getContent)
				.collect(Collectors.joining("\n"));
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemBeerPrompt);

		return systemPromptTemplate.createMessage(Map.of("documents", documents));
	}

}
