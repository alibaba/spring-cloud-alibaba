package com.alibaba.cloud.ai.tongyi;

import com.alibaba.cloud.ai.tongyi.constant.TongyiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@ConfigurationProperties("spring.cloud.ai.tongyi")
public class TongyiProperties {

	private static final Logger logger = LoggerFactory
			.getLogger(TongyiProperties.class);

	/**
	 * Prefix of {@link TongyiProperties}.
	 */
	public static final String PREFIX = "spring.cloud.ai.tongyi";

	/**
	 * Tongyi model api key.
	 */
	private String apiKey;

	/**
	 * Tongyi model name, {@link TongyiConstants}
	 */
	private TongyiConstants modelName;

}
