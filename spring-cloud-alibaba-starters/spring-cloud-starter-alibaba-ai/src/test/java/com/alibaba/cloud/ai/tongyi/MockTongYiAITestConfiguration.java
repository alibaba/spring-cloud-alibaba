package com.alibaba.cloud.ai.tongyi;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.common.MessageManager;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@SpringBootConfiguration
@Profile("spring-ai-tongyi-ai-mocks")
@Import(MockAITestConfiguration.class)
@SuppressWarnings("unused")
public class MockTongYiAITestConfiguration {

	@Bean
	TongYiChatClient tongYiConversationClient() {

		return new TongYiChatClient(new Generation());
	}

	@Bean
	MessageManager messageManager() {

		return new MessageManager();
	}

}
