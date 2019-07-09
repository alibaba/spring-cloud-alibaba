/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.cloud.context.AliCloudConfiguration;

/**
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud")
public class AliCloudProperties implements AliCloudConfiguration {

	/**
	 * alibaba cloud access key.
	 */
	private String accessKey;

	/**
	 * alibaba cloud secret key.
	 */
	private String secretKey;

	@Override
	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	@Override
	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

}
