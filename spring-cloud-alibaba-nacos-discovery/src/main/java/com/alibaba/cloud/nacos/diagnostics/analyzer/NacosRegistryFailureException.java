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

package com.alibaba.cloud.nacos.diagnostics.analyzer;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * A {@code NacosRegistryFailureException} is thrown when the application fails to connect
 * to Nacos Server.
 *
 * @author yuhuangbin
 */
public class NacosRegistryFailureException extends RuntimeException {

	private final Registration registration;

	public NacosRegistryFailureException(Registration registration, Throwable cause) {
		super(cause);
		this.registration = registration;
	}

	public NacosRegistryFailureException(Registration registration, String msg,
			Throwable cause) {
		super(msg, cause);
		this.registration = registration;
	}

	public Registration getRegistration() {
		return registration;
	}

}
