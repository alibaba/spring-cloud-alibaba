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

package com.alibaba.cloud.ai.tongyi.audio.speech;

import com.alibaba.cloud.ai.tongyi.audio.AudioSpeechModels;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

class TongYiAudioSpeechOptionsTests {

	@Test
	public void testSpeechOptions() {

		SpeechSynthesizer mockClient = Mockito.mock(SpeechSynthesizer.class);
		Constants.apiKey = "test";

		var speechClient = new TongYiAudioSpeechModel(mockClient,
				TongYiAudioSpeechOptions.builder()
						.withFormat(SpeechSynthesisAudioFormat.MP3)
						.withRate(333f)
						.withVolume(10)
						.build()
				);

		var tongYiAudioSpeechOptions = speechClient.merge(null);

		assertThat(tongYiAudioSpeechOptions.getModel()).isEqualTo(AudioSpeechModels.SAMBERT_ZHICHU_V1);
		assertThat(tongYiAudioSpeechOptions.getFormat()).isEqualTo(SpeechSynthesisAudioFormat.MP3);
		assertThat(tongYiAudioSpeechOptions.getRate()).isEqualTo(333f);
		assertThat(tongYiAudioSpeechOptions.getVolume()).isEqualTo(10);

		var modelParams = speechClient.toSpeechSynthesisParams(
				TongYiAudioSpeechOptions.builder()
						.withModel("test")
						.withPitch(111f)
						.withVolume(11)
						.withSampleRate(1)
						.build()
		);

		assertThat(modelParams).isInstanceOf(SpeechSynthesisParam.class);
		assertThat(modelParams.getModel()).isEqualTo("test");
		assertThat(modelParams.getVolume()).isEqualTo(11);
		assertThat(modelParams.getSampleRate()).isNotEqualTo(2);

	}

}
