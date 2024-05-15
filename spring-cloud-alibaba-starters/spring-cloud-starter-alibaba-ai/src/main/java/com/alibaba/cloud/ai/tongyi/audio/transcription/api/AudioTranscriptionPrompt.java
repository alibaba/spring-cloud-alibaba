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

package com.alibaba.cloud.ai.tongyi.audio.transcription.api;

import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.ai.tongyi.audio.transcription.TongYiAudioTranscriptionOptions;

/**
 * @author: xYLiu
 * @date: 2024/5/4
 */

public class AudioTranscriptionPrompt {

	private TongYiAudioTranscriptionOptions transcriptionOptions;

	private final AudioUrl audioUrl;

	public AudioTranscriptionPrompt(List<String> urls) {
		this(new AudioUrl(urls), TongYiAudioTranscriptionOptions.builder().build());
	}

	public AudioTranscriptionPrompt(List<String> urls,
			TongYiAudioTranscriptionOptions transcriptionOptions) {
		this(new AudioUrl(urls), transcriptionOptions);
	}

	public AudioTranscriptionPrompt(AudioUrl audioUrl) {
		this.audioUrl = audioUrl;
	}
	public AudioTranscriptionPrompt(AudioUrl audioUrl,
			TongYiAudioTranscriptionOptions transcriptionOptions) {
		this.audioUrl = audioUrl;
		this.transcriptionOptions = transcriptionOptions;
	}

	public AudioUrl getAudioUrl() {
		return audioUrl;
	}

	public TongYiAudioTranscriptionOptions getOptions() {
		return transcriptionOptions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AudioTranscriptionPrompt that = (AudioTranscriptionPrompt) o;
		return Objects.equals(transcriptionOptions, that.transcriptionOptions)
				&& Objects.equals(audioUrl, that.audioUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(transcriptionOptions, audioUrl);
	}

}
