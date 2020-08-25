/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.dubbo.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayConstants.CONFIG_PROPERTY_PREFIX;

/**
 * The Configuration Properties for Dubbo Cloud Gateway
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConfigurationProperties(prefix = CONFIG_PROPERTY_PREFIX)
public class DubboCloudGatewayProperties {

	/**
	 * Enabled or not
	 */
	private boolean enabled = true;

	/**
	 * The context path for the gateway request mapping
	 */
	private String contextPath = "";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
}
