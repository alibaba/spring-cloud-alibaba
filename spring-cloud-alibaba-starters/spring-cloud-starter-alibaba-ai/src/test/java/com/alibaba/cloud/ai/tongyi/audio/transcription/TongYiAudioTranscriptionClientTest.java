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

import java.util.ArrayList;
import java.util.Arrays;

import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionPrompt;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionResponse;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author: xYLiu
 * @date: 2024/5/12
 */

public class TongYiAudioTranscriptionClientTest {
	@Test
	public void test() {
		// Transcription mockClient = Mockito.mock(Transcription.class);
		Transcription mockClient = new Transcription();
		var options = new TongYiAudioTranscriptionOptions();
		var client = new TongYiAudioTranscriptionClient(options, mockClient);

		AudioTranscriptionResponse call = client.call(
				"https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav");

		System.out.println(call.getTranscriptionList());
	}

	@Test
	public void testTranscriptionCall() {
		Transcription mockClient = Mockito.mock(Transcription.class);

		TranscriptionResult expectedResult = new TranscriptionResult();
		expectedResult.setResults(new ArrayList<>());
		when(mockClient.asyncCall(any())).thenReturn(expectedResult);

		var options = new TongYiAudioTranscriptionOptions();
		var client = new TongYiAudioTranscriptionClient(options, mockClient);

		// 假设AudioTranscriptionPrompt需要一个构造函数或工厂方法来创建
		AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(Arrays.asList(
				"https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav"));

		AudioTranscriptionResponse call = client.call(prompt);
	}
}
