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

package com.alibaba.cloud.ai.example.tongyi.service.impl.audio.speech;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import com.alibaba.cloud.ai.tongyi.audio.speech.api.SpeechModel;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

@Service
public class TongYiAudioSimpleServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final SpeechModel speechClient;

	@Autowired
	public TongYiAudioSimpleServiceImpl(SpeechModel client) {

		this.speechClient = client;
	}

	@Override
	public String genAudio(String text) {

		logger.info("gen audio prompt is: {}", text);

		var resWAV = speechClient.call(text);

		return save(resWAV, SpeechSynthesisAudioFormat.WAV.getValue());
	}

	private String save(ByteBuffer audio, String type) {

		String currentPath = System.getProperty("user.dir");
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");
		String fileName = currentPath + File.separator + now.format(formatter) + "." + type;
		File file = new File(fileName);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(audio.array());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return fileName;
	}

}
