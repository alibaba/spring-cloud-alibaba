package com.alibaba.cloud.ai.tongyi.api;

import java.util.function.Consumer;

import com.alibaba.cloud.ai.tongyi.constant.TongyiConstants;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class APIUtils {

	public static final String DEFAULT_BASE_URL = "https://api.openai.com";

	protected static final String DEFAULT_MODEL = TongyiConstants.QWEN_TURBO;

	public static Consumer<HttpHeaders> getJsonContentHeaders(String apiKey) {
		return (headers) -> {
			headers.setBearerAuth(apiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};
	};

}
