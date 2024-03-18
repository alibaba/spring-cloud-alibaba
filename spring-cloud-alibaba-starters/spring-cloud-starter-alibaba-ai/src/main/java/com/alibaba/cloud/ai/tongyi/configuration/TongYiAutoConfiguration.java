package com.alibaba.cloud.ai.tongyi.configuration;

import com.alibaba.cloud.ai.tongyi.client.TongYiChatClient;
import com.alibaba.cloud.ai.tongyi.properties.TongYiProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.common.MessageManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@ConditionalOnClass(Generation.class)
@EnableConfigurationProperties(TongYiProperties.class)
public class TongYiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public TongYiChatClient tongYiChatClient() {

		return new TongYiChatClient(new Generation());
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageManager messageManager() {

		return new MessageManager(10);
	}

}
