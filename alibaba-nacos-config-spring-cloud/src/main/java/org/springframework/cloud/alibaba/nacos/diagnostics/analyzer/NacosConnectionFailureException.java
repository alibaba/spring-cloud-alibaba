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

package org.springframework.cloud.alibaba.nacos.diagnostics.analyzer;

/**
 * A {@code NacosConnectionFailureException} is thrown when the application fails to connect
 * to Nacos Server.
 *
 * @author juven.xuxb
 */
public class NacosConnectionFailureException extends RuntimeException {

	private final String domain;

	private final String port;

	public NacosConnectionFailureException(String domain, String port, String message) {
		super(message);
		this.domain = domain;
		this.port = port;
	}

	public NacosConnectionFailureException(String domain, String port, String message,
										   Throwable cause) {
		super(message, cause);
		this.domain = domain;
		this.port = port;
	}

	String getDomain() {
		return domain;
	}

	String getPort() {
		return port;
	}

}
