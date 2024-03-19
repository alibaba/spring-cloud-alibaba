package com.alibaba.cloud.ai.tongyi;

import com.alibaba.cloud.ai.tongyi.client.TongYiChatClient;
import com.alibaba.dashscope.aigc.generation.Generation;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@AutoConfiguration
@ConditionalOnClass({TongYiChatClient.class})
@EnableConfigurationProperties(TongYiChatProperties.class)
public class TongYiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Generation generation() {

		return new Generation();
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiChatProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiChatClient tongYiChatClient(Generation generation) {

		return new TongYiChatClient(generation);
	}

}
