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

package com.alibaba.cloud.ai.tongyi;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.common.MessageManager;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @since 2023.0.0.0
 */

@AutoConfiguration
@ConditionalOnClass({TongYiChatClient.class, MessageManager.class})
@EnableConfigurationProperties(TongYiChatProperties.class)
public class TongYiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Generation generation() {

		return new Generation();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageManager msgManager() {

		return new MessageManager(10);
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiChatProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiChatClient tongYiChatClient(Generation generation, TongYiChatProperties chatOptions) {

		return new TongYiChatClient(generation, chatOptions.getOptions());
	}

}
