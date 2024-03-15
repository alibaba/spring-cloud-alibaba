package com.alibaba.cloud.ai.tongyi.api;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongyChatiAPI {

	public TongyChatiAPI(String apiKey) {

		this(APIUtils.DEFAULT_BASE_URL, apiKey);
	}

	public TongyChatiAPI(String defaultBaseUrl, String apiKey) {

		// todo: client type.
		this(defaultBaseUrl, apiKey, clientType);
	}
}
