package com.alibaba.cloud.ai.tongyi.configuration;

import com.alibaba.cloud.ai.tongyi.client.TongYiChatClient;
import com.alibaba.cloud.ai.tongyi.properties.TongYiPropertiesOptions;
import com.alibaba.dashscope.aigc.generation.Generation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@ConditionalOnClass(Generation.class)
@EnableConfigurationProperties(TongYiPropertiesOptions.class)
public class TongYiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public TongYiChatClient tongYiChatClient() {

		return new TongYiChatClient();
	}

}
