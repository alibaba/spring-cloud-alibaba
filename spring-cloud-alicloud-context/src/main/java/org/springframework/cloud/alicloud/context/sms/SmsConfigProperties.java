package org.springframework.cloud.alicloud.context.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @author pbting
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsConfigProperties implements Serializable {

	// 产品名称:云通信短信API产品,开发者无需替换
	public static final String smsProduct = "Dysmsapi";
	// 产品域名,开发者无需替换
	public static final String smsDomain = "dysmsapi.aliyuncs.com";

	private AliCloudProperties aliCloudProperties;

	/**
	 *
	 */
	private String reportQueueName;
	/**
	 *
	 */
	private String upQueueName;

	/**
	 *
	 */
	protected String connnectTimeout = "10000";

	/**
	 *
	 */
	protected String readTimeout = "10000";

	public SmsConfigProperties(AliCloudProperties aliCloudProperties) {
		this.aliCloudProperties = aliCloudProperties;
	}

	public String getConnnectTimeout() {
		return connnectTimeout;
	}

	public void setConnnectTimeout(String connnectTimeout) {
		this.connnectTimeout = connnectTimeout;
	}

	public String getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(String readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void overiideFromEnv(Environment environment) {
		overiideCustomFromEnv(environment);
		if (StringUtils.isEmpty(connnectTimeout)) {
			String resolveResult = environment.resolveRequiredPlaceholders(
					"${spring.cloud.alibaba.sms.connect-timeout:}");
			this.setConnnectTimeout(
					StringUtils.isEmpty(resolveResult) ? "10000" : resolveResult);
		}

		if (StringUtils.isEmpty(readTimeout)) {
			String resolveResult = environment.resolveRequiredPlaceholders(
					"${spring.cloud.alibaba.sms.read-timeout:}");
			this.setReadTimeout(
					StringUtils.isEmpty(resolveResult) ? "10000" : resolveResult);
		}
	}

	public void overiideCustomFromEnv(Environment environment) {
		// nothing to do
	}

	public String getReportQueueName() {
		return reportQueueName;
	}

	public void setReportQueueName(String reportQueueName) {
		this.reportQueueName = reportQueueName;
	}

	public String getUpQueueName() {
		return upQueueName;
	}

	public String getAccessKeyId() {
		return aliCloudProperties.getAccessKey();
	}

	public String getAccessKeySecret() {
		return aliCloudProperties.getSecretKey();
	}

	public void setUpQueueName(String upQueueName) {
		this.upQueueName = upQueueName;
	}

}