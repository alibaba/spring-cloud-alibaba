package com.alibaba.cloud.ai.example.tongyi.service.impl.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import com.alibaba.cloud.ai.tongyi.audio.api.SpeechClient;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Slf4j
@Service
public class TongYiAudioSimpleServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final SpeechClient speechClient;

	@Autowired
	public TongYiAudioSimpleServiceImpl(SpeechClient client) {

		this.speechClient = client;
	}

	@Override
	public String genAudio(String text) {

		logger.info("gen audio prompt is: {}", text);
		var resWAV = speechClient.call(text);

		return save(resWAV, SpeechSynthesisAudioFormat.MP3.getValue());
	}

	private String save(ByteBuffer audio, String type) {

		String currentPath = System.getProperty("user.dir");
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");
		String fileName = currentPath + File.separator + now.format(formatter) + "." + type;
		File file = new File(fileName);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(audio.array());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return fileName;
	}

}