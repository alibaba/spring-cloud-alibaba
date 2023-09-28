/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.mtls;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.commons.governance.tls.ServerTlsModeHolder;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
@ConfigurationProperties(MtlsConfigProperties.PREFIX)
public class MtlsConfigProperties {

	private Boolean serverTls;

	/**
	 * Prefix for mtls config.
	 */
	public static final String PREFIX = "spring.cloud.mtls.config";

	public void setServerTls(boolean serverTls) {
		this.serverTls = serverTls;
	}

	public boolean isServerTls() {
		return serverTls;
	}

	@PostConstruct
	public void postConstruct() {
		if (serverTls == null) {
			serverTls = true;
		}
		ServerTlsModeHolder.init(serverTls);
	}

}
