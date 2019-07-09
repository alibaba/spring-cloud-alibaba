package org.springframework.cloud.alicloud.context.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pbting
 * @author xiaolongzuo
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsProperties {

	/**
	 * Product name.
	 */
	public static final String smsProduct = "Dysmsapi";

	/**
	 * Product domain.
	 */
	public static final String smsDomain = "dysmsapi.aliyuncs.com";

	/**
	 * Report queue name.
	 */
	private String reportQueueName;

	/**
	 * Up queue name.
	 */
	private String upQueueName;

	/**
	 * Connect timeout.
	 */
	private String connectTimeout = "10000";

	/**
	 * Read timeout.
	 */
	private String readTimeout = "10000";

	public String getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(String connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public String getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(String readTimeout) {
		this.readTimeout = readTimeout;
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

	public void setUpQueueName(String upQueueName) {
		this.upQueueName = upQueueName;
	}

}