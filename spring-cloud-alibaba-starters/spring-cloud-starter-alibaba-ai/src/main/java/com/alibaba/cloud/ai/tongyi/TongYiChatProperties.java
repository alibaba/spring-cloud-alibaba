package com.alibaba.cloud.ai.tongyi;

import com.alibaba.cloud.ai.tongyi.constant.TongYiConstants;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@ConfigurationProperties(TongYiChatProperties.CONFIG_PREFIX)
public class TongYiChatProperties {

	public static final String CONFIG_PREFIX = "spring.cloud.ai.tongyi.chat";

	public static final String DEFAULT_DEPLOYMENT_NAME = TongYiConstants.Model.QWEN_TURBO;

	private static final Double DEFAULT_TEMPERATURE = 0.8;

	/**
	 * Enable TongYiQWEN ai chat client.
	 */
	private boolean enabled = true;

	private Integer test;

	public Integer getTest() {

		return test;
	}

	public void setTest(Integer test) {
		this.test = test;
	}

	@NestedConfigurationProperty
	private TongYiChatOptions options = TongYiChatOptions.builder()
			.withModel(DEFAULT_DEPLOYMENT_NAME)
			.withTemperature(DEFAULT_TEMPERATURE)
			.withEnableSearch(true)
			.withResultFormat(QwenParam.ResultFormat.MESSAGE)
			.build();

	public TongYiChatOptions getOptions() {
		System.out.println("fafdsf: " + test);
		System.out.println("sdd  : " + options.getApiKey());

		return this.options;
	}

	public void setOptions(TongYiChatOptions options) {

		this.options = options;
	}

	public boolean isEnabled() {

		return this.enabled;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
	}

}
