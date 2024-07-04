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

import java.nio.ByteBuffer;

import com.alibaba.cloud.ai.tongyi.audio.speech.api.SpeechPrompt;
import com.alibaba.cloud.ai.tongyi.audio.speech.api.SpeechResponse;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import io.reactivex.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

class TongYiAudioSpeechClientTest {

	@Mock
	private SpeechSynthesizer speechSynthesizer;

	private TongYiAudioSpeechModel client;

	@BeforeEach
	void setUp() {

		MockitoAnnotations.openMocks(this);
		client = new TongYiAudioSpeechModel(speechSynthesizer, new TongYiAudioSpeechOptions());
	}

	@Test
	void shouldReturnSpeechResponseWhenCallWithText() {

		ByteBuffer buffer = ByteBuffer.allocate(10);
		when(speechSynthesizer.call(any())).thenReturn(buffer);
		ByteBuffer result = client.call("test");

		assertThat(result).isEqualTo(buffer);
	}

	@Test
	void shouldReturnSpeechResponseWhenCallWithSpeechPrompt() {

		SpeechPrompt prompt = new SpeechPrompt("test");
		SpeechSynthesisResult synthesisResult = new SpeechSynthesisResult();
		synthesisResult.setAudioFrame(ByteBuffer.allocate(10));

		when(speechSynthesizer.call(any())).thenReturn(synthesisResult.getAudioFrame());
		SpeechResponse result = client.call(prompt);

		assertThat(result.getResult().getOutput()).isEqualTo(synthesisResult.getAudioFrame());
	}

	@Test
	void shouldReturnFluxOfSpeechResponseWhenStreamWithSpeechPrompt() {

		SpeechPrompt prompt = new SpeechPrompt("test");
		SpeechSynthesisResult synthesisResult = new SpeechSynthesisResult();
		synthesisResult.setAudioFrame(ByteBuffer.allocate(10));

		when(speechSynthesizer.streamCall(any())).thenReturn(Flowable.just(synthesisResult));
		Flux<SpeechResponse> result = client.stream(prompt);
		StepVerifier.create(result)
				.expectNextMatches(response -> response.getResult().getOutput().equals(synthesisResult.getAudioFrame()))
				.verifyComplete();

	}

}
