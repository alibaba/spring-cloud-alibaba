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

package org.springframework.cloud.alibaba.sentinel;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.Ordered;

/**
 * @author xiaojing
 * @author hengyunabc
 */
@ConfigurationProperties(prefix = "spring.cloud.sentinel")
public class SentinelProperties {

	/**
	 * Enable sentinel auto configure, the default value is true
	 */
	private boolean enabled = true;

	/**
	 * sentinel api port,default value is 8721
	 */
	private String port = "8721";

	/**
	 * Sentinel dashboard address, won't try to connect dashboard when address is empty
	 */
	private String dashboard = "";

	@NestedConfigurationProperty
	private Filter filter;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDashboard() {
		return dashboard;
	}

	public void setDashboard(String dashboard) {
		this.dashboard = dashboard;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public static class Filter {

		/**
		 * Sentinel filter chain order.
		 */
		private int order = Ordered.HIGHEST_PRECEDENCE;

		/**
		 * URL pattern for sentinel filter,default is /*
		 */
		private List<String> urlPatterns;

		public int getOrder() {
			return this.order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public List<String> getUrlPatterns() {
			return urlPatterns;
		}

		public void setUrlPatterns(List<String> urlPatterns) {
			this.urlPatterns = urlPatterns;
		}
	}
}
