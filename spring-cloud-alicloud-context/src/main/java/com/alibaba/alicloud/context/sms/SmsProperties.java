/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.alicloud.context.sms;

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
	public static final String SMS_PRODUCT = "Dysmsapi";

	/**
	 * Product domain.
	 */
	public static final String SMS_DOMAIN = "dysmsapi.aliyuncs.com";

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
