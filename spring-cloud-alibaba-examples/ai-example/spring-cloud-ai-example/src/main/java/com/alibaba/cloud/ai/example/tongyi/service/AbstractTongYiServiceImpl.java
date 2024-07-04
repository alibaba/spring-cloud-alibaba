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

import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.models.ActorsFilms;
import com.alibaba.cloud.ai.example.tongyi.models.Completion;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.image.ImageResponse;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0
 */

public abstract class AbstractTongYiServiceImpl implements TongYiService {

	private static final String INFO_PREFIX = "please implement ";
	private static final String INFO_SUFFIX = "() method.";

	@Override
	public String completion(String message) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread().getStackTrace()[2].getMethodName());
	}

	@Override
	public Map<String, String> streamCompletion(String message) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public ActorsFilms genOutputParse(String actor) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public AssistantMessage genPromptTemplates(String adjective, String topic) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public AssistantMessage genRole(String message, String name, String voice) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public Completion stuffCompletion(String message, boolean stuffit) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public ImageResponse genImg(String imgPrompt) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public String genAudio(String text) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public List<Double> textEmbedding(String text) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

	@Override
	public String audioTranscription(String url) {

		throw new RuntimeException(INFO_PREFIX + Thread.currentThread()
				.getStackTrace()[2].getMethodName() + INFO_SUFFIX);
	}

}
