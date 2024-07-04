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

package com.alibaba.cloud.ai.example.tongyi.service.impl.audio.transcription;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import com.alibaba.cloud.ai.tongyi.audio.transcription.TongYiAudioTranscriptionModel;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionPrompt;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

/**
 * @author xYLiu
 * @author yuluo
 * @since 2023.0.1.0
 */

@Service
public class TongYiAudioTranscriptionServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final TongYiAudioTranscriptionModel audioTranscriptionModel;

	@Autowired
	public TongYiAudioTranscriptionServiceImpl(final TongYiAudioTranscriptionModel transcriptionModel) {

		this.audioTranscriptionModel = transcriptionModel;
	}

	@Override
	public String audioTranscription(String audioUrls) {

		Resource resource;

		try {
			resource = new UrlResource(audioUrls);
		}
		catch (IOException e) {
			logger.error("Failed to create resource.");
			throw new RuntimeException(e);
		}
		AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(resource);

		return save(audioTranscriptionModel.call(audioTranscriptionPrompt).getResults());
	}

	private String save(List<AudioTranscriptionResult> resultList) {
		String currentPath = System.getProperty("user.dir");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");
		StringBuilder retPaths = new StringBuilder();
		for (AudioTranscriptionResult audioTranscriptionResult : resultList) {
			String tUrl = audioTranscriptionResult.getOutput();
			LocalDateTime now = LocalDateTime.now();
			String fileName = currentPath + File.separator + now.format(formatter) + ".txt";
			retPaths.append(fileName).append("\n");
			try {
				URL url = new URL(tUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				StringBuilder sb = new StringBuilder();
				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream()); FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
						byte[] dataBuffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
							sb.append(new String(dataBuffer, 0, bytesRead));
						}
						JsonObject rootObj = JsonParser.parseString(sb.toString()).getAsJsonObject();
						JsonArray transcriptsArray = rootObj.getAsJsonArray("transcripts");

						for (var transcriptElement : transcriptsArray) {
							JsonObject transcriptObj = transcriptElement.getAsJsonObject();
							String text = transcriptObj.get("text").getAsString();
							fileOutputStream.write(text.getBytes());
						}
						logger.info("File downloaded successfully：{}\n", fileName);
					}
				}
				else {
					logger.error("The download failed, and the response code：{}",
							responseCode);
				}
				connection.disconnect();
			}
			catch (IOException e) {
				logger.error("An error occurred during the file download process.");
			}
		}
		return retPaths.toString();
	}
}
