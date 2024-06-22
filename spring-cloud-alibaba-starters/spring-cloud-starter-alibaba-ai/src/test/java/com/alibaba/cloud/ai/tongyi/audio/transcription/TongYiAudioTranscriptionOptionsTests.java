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

package com.alibaba.cloud.ai.tongyi.audio.transcription;

import com.alibaba.cloud.ai.tongyi.audio.AudioTranscriptionModels;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionParam;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xYLiu
 * @author yuluo
 * @since 2023.0.1.0
 */

class TongYiAudioTranscriptionOptionsTests {

	@Test
	void testTranscriptionOptions() {

		Transcription mockClient = Mockito.mock(Transcription.class);
		Constants.apiKey = "test";

		var transcription = new TongYiAudioTranscriptionModel(
				TongYiAudioTranscriptionOptions.builder().withDiarizationEnabled(false)
						.withAudioEventDetectionEnabled(false)
						.withTimestampAlignmentEnabled(false)
						.withDisfluencyRemovalEnabled(false).build(),
				mockClient);

		var tongYiAudioTranscriptionOptions = transcription.merge(null);

		assertThat(tongYiAudioTranscriptionOptions.getModel())
				.isEqualTo(AudioTranscriptionModels.Paraformer_V1);
		assertThat(tongYiAudioTranscriptionOptions.getDiarizationEnabled())
				.isEqualTo(false);
		assertThat(tongYiAudioTranscriptionOptions.getAudioEventDetectionEnabled())
				.isEqualTo(false);
		assertThat(tongYiAudioTranscriptionOptions.getTimestampAlignmentEnabled())
				.isEqualTo(false);
		assertThat(tongYiAudioTranscriptionOptions.getDisfluencyRemovalEnabled())
				.isEqualTo(false);

		var modelParams = transcription.toTranscriptionParam(TongYiAudioTranscriptionOptions
						.builder()
						.withModel("test")
						.withSpeakerCount(2)
						.build()
				);

		assertThat(modelParams).isInstanceOf(TranscriptionParam.class);
		assertThat(modelParams.getModel()).isEqualTo("test");
		assertThat(modelParams.getSpeakerCount()).isEqualTo(2);
	}
}
