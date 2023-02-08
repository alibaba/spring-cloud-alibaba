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

package com.alibaba.cloud.governance.opensergo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
@ConfigurationProperties(OpenSergoConfigProperties.PREFIX)
public class OpenSergoConfigProperties {

	/**
	 * Prefix in yaml.
	 */
	public static final String PREFIX = "spring.cloud.opensergo";

	/**
	 * Configurations about OpenSergo Server Endpoint.
	 */
	private String endpoint;

	/**
	 * Namespace Configuration about OpenSergo Config.
	 */
	private String namespace = "default";

	public String getNamespace() {
		return namespace;
	}

	void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
