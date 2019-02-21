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

package org.springframework.cloud.alicloud.context.edas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.cloud.context.edas.EdasConfiguration;

/**
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.edas")
public class EdasProperties implements EdasConfiguration {

	private static final String DEFAULT_APPLICATION_NAME = "";

	/**
	 * edas application name.
	 */
	@Value("${spring.application.name:${spring.cloud.alicloud.edas.application.name:}}")
	private String applicationName;

	/**
	 * edas namespace
	 */
	private String namespace;

	/**
	 * whether or not connect edas.
	 */
	private boolean enabled;

	@Override
	public String getRegionId() {
		if (namespace == null) {
			return null;
		}
		return namespace.contains(":") ? namespace.split(":")[0] : namespace;
	}

	@Override
	public boolean isApplicationNameValid() {
		return !DEFAULT_APPLICATION_NAME.equals(applicationName);
	}

	@Override
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
