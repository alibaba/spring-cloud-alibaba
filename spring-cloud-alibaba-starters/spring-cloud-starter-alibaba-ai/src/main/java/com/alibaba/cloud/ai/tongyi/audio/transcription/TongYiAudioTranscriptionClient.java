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
import java.util.List;

import com.alibaba.cloud.ai.tongyi.audio.ParaformerModels;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionPrompt;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionResponse;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.TranscriptionClient;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionQueryParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionResult;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionTaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;

/**
 * TongYiAudioTranscriptionClient is a client for TongYi audio transcription service for
 * Spring Cloud Alibaba AI.
 * @author: xYLiu
 * @date: 2024/5/4
 */

public class TongYiAudioTranscriptionClient implements TranscriptionClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TongYi models options.
	 */
	private final TongYiAudioTranscriptionOptions defaultOptions;

	/**
	 * TongYi models api.
	 */
	private final Transcription transcription;

	public TongYiAudioTranscriptionClient(Transcription transcription) {
		this(null, transcription);
	}

	public TongYiAudioTranscriptionClient(TongYiAudioTranscriptionOptions defaultOptions,
			Transcription transcription) {
		Assert.notNull(transcription, "transcription must not be null");
		Assert.notNull(defaultOptions, "defaultOptions must not be null");

		this.defaultOptions = defaultOptions;
		this.transcription = transcription;
	}

	@Override
	public AudioTranscriptionResponse call(AudioTranscriptionPrompt prompt) {
		var param = merge(prompt.getOptions());
		List<String> urls = prompt.getAudioUrl().getfileUrls();
		TranscriptionParam transcriptionParam = toTranscriptionParam(param);
		transcriptionParam.setFileUrls(urls);
		transcriptionParam.setApiKey("sk-0e6c387446ff45d0924111475a82462e");
		logger.info(transcriptionParam.toString());

		List<TranscriptionTaskResult> taskResultList = null;
		try {
			// Submit a transcription request
			TranscriptionResult result = transcription.asyncCall(transcriptionParam);
			// Wait for the transcription to complete
			result = transcription.wait(TranscriptionQueryParam
					.FromTranscriptionParam(transcriptionParam, result.getTaskId()));
			// Get the transcription results
			taskResultList = result.getResults();
			return new AudioTranscriptionResponse(taskResultList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new AudioTranscriptionResponse(taskResultList);
	}

	public TongYiAudioTranscriptionOptions merge(TongYiAudioTranscriptionOptions target) {
		var mergeBuilder = TongYiAudioTranscriptionOptions.builder();

		mergeBuilder
				.withModel(defaultOptions.getModel() != null ? defaultOptions.getModel()
						: target.getModel());
		mergeBuilder.withChannelId(
				defaultOptions.getChannelId() != null ? defaultOptions.getChannelId()
						: target.getChannelId());
		mergeBuilder.withDiarizationEnabled(defaultOptions.getDiarizationEnabled() != null
				? defaultOptions.getDiarizationEnabled()
				: target.getDiarizationEnabled());
		mergeBuilder.withDisfluencyRemovalEnabled(
				defaultOptions.getDisfluencyRemovalEnabled() != null
						? defaultOptions.getDisfluencyRemovalEnabled()
						: target.getDisfluencyRemovalEnabled());
		mergeBuilder.withTimestampAlignmentEnabled(
				defaultOptions.getTimestampAlignmentEnabled() != null
						? defaultOptions.getTimestampAlignmentEnabled()
						: target.getTimestampAlignmentEnabled());
		mergeBuilder.withSpecialWordFilter(defaultOptions.getSpecialWordFilter() != null
				? defaultOptions.getSpecialWordFilter()
				: target.getSpecialWordFilter());
		mergeBuilder.withAudioEventDetectionEnabled(
				defaultOptions.getAudioEventDetectionEnabled() != null
						? defaultOptions.getAudioEventDetectionEnabled()
						: target.getAudioEventDetectionEnabled());

		return mergeBuilder.build();
	}

	public TranscriptionParam toTranscriptionParam(
			TongYiAudioTranscriptionOptions source) {
		var mergeBuilder = TranscriptionParam.builder();

		mergeBuilder.model(source.getModel() != null ? source.getModel()
				: ParaformerModels.Paraformer_V1);
		mergeBuilder.fileUrls(
				source.getFileUrls() != null ? source.getFileUrls() : new ArrayList<>());

		if (source.getPhraseId() != null) {
			mergeBuilder.phraseId(source.getPhraseId());
		}
		if (source.getChannelId() != null) {
			mergeBuilder.channelId(source.getChannelId());
		}
		if (source.getDiarizationEnabled() != null) {
			mergeBuilder.diarizationEnabled(source.getDiarizationEnabled());
		}
		if (source.getSpeakerCount() != null) {
			mergeBuilder.speakerCount(source.getSpeakerCount());
		}
		if (source.getDisfluencyRemovalEnabled() != null) {
			mergeBuilder.disfluencyRemovalEnabled(source.getDisfluencyRemovalEnabled());
		}
		if (source.getTimestampAlignmentEnabled() != null) {
			mergeBuilder.timestampAlignmentEnabled(source.getTimestampAlignmentEnabled());
		}
		if (source.getSpecialWordFilter() != null) {
			mergeBuilder.specialWordFilter(source.getSpecialWordFilter());
		}
		if (source.getAudioEventDetectionEnabled() != null) {
			mergeBuilder
					.audioEventDetectionEnabled(source.getAudioEventDetectionEnabled());
		}

		return mergeBuilder.build();
	}

}
