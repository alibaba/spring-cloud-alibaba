package com.alibaba.cloud.ai.example.tongyi.service.impl;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Slf4j
@Service
public class TongYiServiceImpl implements TongYiService {

	private final ChatClient chatClient;

	@Autowired
	public TongYiServiceImpl(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public Map<String, String> completion(String message) {

		return Map.of(message, chatClient.call(message));
	}
}
