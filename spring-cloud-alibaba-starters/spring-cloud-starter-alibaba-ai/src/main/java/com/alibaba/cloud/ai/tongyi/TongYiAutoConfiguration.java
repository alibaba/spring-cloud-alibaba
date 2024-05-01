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

import java.util.Objects;

import com.alibaba.cloud.ai.tongyi.audio.TongYiAudioSpeechClient;
import com.alibaba.cloud.ai.tongyi.audio.TongYiAudioSpeechProperties;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatClient;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatProperties;
import com.alibaba.cloud.ai.tongyi.constants.TongYiConstants;
import com.alibaba.cloud.ai.tongyi.exception.TongYiException;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesClient;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.ApiKey;
import com.alibaba.dashscope.utils.Constants;

import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@AutoConfiguration
@ConditionalOnClass({
		MessageManager.class,
		TongYiChatClient.class,
		TongYiImagesClient.class,
		TongYiAudioSpeechClient.class
})
@EnableConfigurationProperties({
		TongYiChatProperties.class,
		TongYiImagesProperties.class,
		TongYiAudioSpeechProperties.class,
		TongYiConnectionProperties.class
})
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
	@ConditionalOnMissingBean
	public ImageSynthesis imageSynthesis() {

		return new ImageSynthesis();
	}

	@Bean
	@ConditionalOnMissingBean
	public SpeechSynthesizer speechSynthesizer() {

		return new SpeechSynthesizer();
	}

	@Bean
	@ConditionalOnMissingBean
	public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {

		FunctionCallbackContext manager = new FunctionCallbackContext();
		manager.setApplicationContext(context);

		return manager;
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiChatProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiChatClient tongYiChatClient(Generation generation,
			TongYiChatProperties chatOptions,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiChatClient(generation, chatOptions.getOptions());
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiImagesProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiImagesClient tongYiImagesClient(
			ImageSynthesis imageSynthesis,
			TongYiImagesProperties imagesOptions,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiImagesClient(imageSynthesis, imagesOptions.getOptions());
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiAudioSpeechProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiAudioSpeechClient tongYiAudioSpeechClient(
			SpeechSynthesizer speechSynthesizer,
			TongYiAudioSpeechProperties speechProperties,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiAudioSpeechClient(speechSynthesizer, speechProperties.getOptions());
	}

	/**
	 * Setting the API key.
	 * @param connectionProperties {@link TongYiConnectionProperties}
	 */
	private void settingApiKey(TongYiConnectionProperties connectionProperties) {

		String apiKey;

		try {
			// It is recommended to set the key by defining the api-key in an environment variable.
			var envKey = System.getenv(TongYiConstants.SCA_AI_TONGYI_API_KEY);
			if (Objects.nonNull(envKey)) {
				Constants.apiKey = envKey;
				return;
			}
			if (Objects.nonNull(connectionProperties.getApiKey())) {
				apiKey = connectionProperties.getApiKey();
			}
			else {
				apiKey = ApiKey.getApiKey(null);
			}

			Constants.apiKey = apiKey;
		}
		catch (NoApiKeyException e) {

			throw new TongYiException(e.getMessage());
		}

	}

}
