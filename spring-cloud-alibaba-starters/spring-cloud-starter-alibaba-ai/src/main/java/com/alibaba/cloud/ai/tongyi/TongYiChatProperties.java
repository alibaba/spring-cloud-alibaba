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

import com.alibaba.cloud.ai.tongyi.constant.TongYiConstants;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author yuluo
 * @since 2023.0.0.0
 */

@ConfigurationProperties(TongYiChatProperties.CONFIG_PREFIX)
public class TongYiChatProperties {

	/**
	 * Spring Cloud Alibaba AI configuration prefix.
	 */
	public static final String CONFIG_PREFIX = "spring.cloud.ai.tongyi.chat";

	/**
	 * Default TongYi Chat model.
	 */
	public static final String DEFAULT_DEPLOYMENT_NAME = TongYiConstants.Model.QWEN_TURBO;

	/**
	 * Default temperature speed.
	 */
	private static final Double DEFAULT_TEMPERATURE = 0.8;

	/**
	 * Enable TongYiQWEN ai chat client.
	 */
	private boolean enabled = true;

	@NestedConfigurationProperty
	private TongYiChatOptions options = TongYiChatOptions.builder()
			.withModel(DEFAULT_DEPLOYMENT_NAME)
			.withTemperature(DEFAULT_TEMPERATURE)
			.withEnableSearch(true)
			.withResultFormat(QwenParam.ResultFormat.MESSAGE)
			.build();

	public TongYiChatOptions getOptions() {

		return this.options;
	}

	public void setOptions(TongYiChatOptions options) {

		this.options = options;
	}

	public boolean isEnabled() {

		return this.enabled;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
	}

}
