package com.alibaba.cloud.ai.example.tongyi.service;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.models.ActorsFilms;
import com.alibaba.cloud.ai.example.tongyi.models.Completion;

import org.springframework.ai.chat.messages.AssistantMessage;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

public abstract class AbstractTongYiServiceImpl implements TongYiService {

	@Override
	public String completion(String message) {

		return null;
	}

	@Override
	public Map<String, String> streamCompletion(String message) {

		return null;
	}

	@Override
	public ActorsFilms genOutputParse(String actor) {

		return null;
	}

	@Override
	public AssistantMessage genPromptTemplates(String adjective, String topic) {

		return null;
	}

	@Override
	public AssistantMessage genRole(String message, String name, String voice) {

		return null;
	}

	@Override
	public Completion stuffCompletion(String message, boolean stuffit) {

		return null;
	}
}
