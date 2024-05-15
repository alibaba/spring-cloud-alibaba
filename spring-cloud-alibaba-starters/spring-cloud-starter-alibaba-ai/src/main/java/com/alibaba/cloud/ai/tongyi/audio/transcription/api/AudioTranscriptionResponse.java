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

import com.alibaba.dashscope.audio.asr.transcription.TranscriptionTaskResult;



/**
 * @author: xYLiu
 * @date: 2024/5/4
 */

public class AudioTranscriptionResponse {
	private final List<TranscriptionTaskResult> transcriptionList;

	public AudioTranscriptionResponse(List<TranscriptionTaskResult> transcriptionList) {
		this.transcriptionList = transcriptionList;
	}

	public List<TranscriptionTaskResult> getTranscriptionList() {
		return transcriptionList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AudioTranscriptionResponse that = (AudioTranscriptionResponse) o;
		return Objects.equals(transcriptionList, that.transcriptionList);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(transcriptionList);
	}
}
