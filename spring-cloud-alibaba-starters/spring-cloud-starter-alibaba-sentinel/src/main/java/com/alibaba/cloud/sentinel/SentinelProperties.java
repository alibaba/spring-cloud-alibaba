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

package com.alibaba.cloud.sentinel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.cloud.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * {@link ConfigurationProperties} for Sentinel.
 *
 * @author xiaojing
 * @author hengyunabc
 * @author jiashuai.xie
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = SentinelConstants.PROPERTY_PREFIX)
@Validated
public class SentinelProperties {

	/**
	 * Earlier initialize heart-beat when the spring container starts when the transport
	 * dependency is on classpath, the configuration is effective.
	 */
	private boolean eager = false;

	/**
	 * Enable sentinel auto configure, the default value is true.
	 */
	private boolean enabled = true;

	/**
	 * The process page when the flow control is triggered.
	 */
	private String blockPage;

	/**
	 * Configurations about datasource, like 'nacos', 'apollo', 'file', 'zookeeper'.
	 */
	private Map<String, DataSourcePropertiesConfiguration> datasource = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);

	/**
	 * Transport configuration about dashboard and client.
	 */
	private Transport transport = new Transport();

	/**
	 * Metric configuration about resource.
	 */
	private Metric metric = new Metric();

	/**
	 * Web servlet configuration when the application is web, the configuration is
	 * effective.
	 */
	private Servlet servlet = new Servlet();

	/**
	 * Sentinel interceptor when the application is web, the configuration is effective.
	 */
	private Filter filter = new Filter();

	/**
	 * Sentinel Flow configuration.
	 */
	private Flow flow = new Flow();

	/**
	 * Sentinel log configuration {@link LogBase}.
	 */
	private Log log = new Log();

	/**
	 * Add HTTP method prefix for Sentinel Resource.
	 */
	private Boolean httpMethodSpecify = false;

	/**
	 * Specify whether unify web context(i.e. use the default context name), and is true
	 * by default.
	 */
	private Boolean webContextUnify = true;

	public Boolean getWebContextUnify() {
		return webContextUnify;
	}

	public void setWebContextUnify(Boolean webContextUnify) {
		this.webContextUnify = webContextUnify;
	}

	public boolean isEager() {
		return eager;
	}

	public void setEager(boolean eager) {
		this.eager = eager;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public Map<String, DataSourcePropertiesConfiguration> getDatasource() {
		return datasource;
	}

	public void setDatasource(Map<String, DataSourcePropertiesConfiguration> datasource) {
		this.datasource = datasource;
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public Boolean getHttpMethodSpecify() {
		return httpMethodSpecify;
	}

	public void setHttpMethodSpecify(Boolean httpMethodSpecify) {
		this.httpMethodSpecify = httpMethodSpecify;
	}

	public String getBlockPage() {
		if (StringUtils.hasText(this.blockPage)) {
			return this.blockPage;
		}
		return this.servlet.getBlockPage();
	}

	public void setBlockPage(String blockPage) {
		this.blockPage = blockPage;
	}

	public static class Flow {

		/**
		 * The cold factor {@link SentinelConfig#COLD_FACTOR}.
		 */
		private String coldFactor = SentinelConstants.COLD_FACTOR;

		public String getColdFactor() {
			return coldFactor;
		}

		public void setColdFactor(String coldFactor) {
			this.coldFactor = coldFactor;
		}

	}

	public static class Servlet {

		/**
		 * The process page when the flow control is triggered.
		 */
		private String blockPage;

		@Deprecated
		@DeprecatedConfigurationProperty(
				reason = "replaced to SentinelProperties#blockPage.",
				replacement = SentinelConstants.PROPERTY_PREFIX + ".block-page")
		public String getBlockPage() {
			return blockPage;
		}

		@Deprecated
		public void setBlockPage(String blockPage) {
			this.blockPage = blockPage;
		}

	}

	public static class Metric {

		/**
		 * The metric file size {@link SentinelConfig#SINGLE_METRIC_FILE_SIZE}.
		 */
		private String fileSingleSize;

		/**
		 * The total metric file count {@link SentinelConfig#TOTAL_METRIC_FILE_COUNT}.
		 */
		private String fileTotalCount;

		/**
		 * Charset when sentinel write or search metric file.
		 * {@link SentinelConfig#CHARSET}
		 */
		private String charset = SentinelConstants.CHARSET;

		public String getFileSingleSize() {
			return fileSingleSize;
		}

		public void setFileSingleSize(String fileSingleSize) {
			this.fileSingleSize = fileSingleSize;
		}

		public String getFileTotalCount() {
			return fileTotalCount;
		}

		public void setFileTotalCount(String fileTotalCount) {
			this.fileTotalCount = fileTotalCount;
		}

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

	}

	public static class Transport {

		/**
		 * Sentinel api port, default value is 8719 {@link TransportConfig#SERVER_PORT}.
		 */
		private String port = SentinelConstants.API_PORT;

		/**
		 * Sentinel dashboard address, won't try to connect dashboard when address is
		 * empty {@link TransportConfig#CONSOLE_SERVER}.
		 */
		private String dashboard = "";

		/**
		 * Sentinel dashboard heartbeat api path, default value is '/registry/machine'.
		 * {@link TransportConfig#HEARTBEAT_API_PATH}.
		 */
		private String apiPath;

		/**
		 * Send heartbeat interval millisecond
		 * {@link TransportConfig#HEARTBEAT_INTERVAL_MS}.
		 */
		private String heartbeatIntervalMs;

		/**
		 * Get heartbeat client local ip. If the client ip not configured, it will be the
		 * address of local host.
		 */
		private String clientIp;

		public String getHeartbeatIntervalMs() {
			return heartbeatIntervalMs;
		}

		public void setHeartbeatIntervalMs(String heartbeatIntervalMs) {
			this.heartbeatIntervalMs = heartbeatIntervalMs;
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

		public String getApiPath() {
			return apiPath;
		}

		public void setApiPath(String apiPath) {
			this.apiPath = apiPath;
		}

		public String getClientIp() {
			return clientIp;
		}

		public void setClientIp(String clientIp) {
			this.clientIp = clientIp;
		}

	}

	public static class Filter {

		/**
		 * SentinelWebInterceptor order, will be register to InterceptorRegistry.
		 */
		private int order = Ordered.HIGHEST_PRECEDENCE;

		/**
		 * URL pattern for SentinelWebInterceptor, default is /**.
		 */
		private List<String> urlPatterns = Arrays.asList("/**");

		/**
		 * Enable to instance
		 * {@link com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor}.
		 */
		private boolean enabled = true;

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

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	public static class Log {

		/**
		 * Sentinel log base dir.
		 */
		private String dir;

		/**
		 * Distinguish the log file by pid number.
		 */
		private boolean switchPid = false;

		public String getDir() {
			return dir;
		}

		public void setDir(String dir) {
			this.dir = dir;
		}

		public boolean isSwitchPid() {
			return switchPid;
		}

		public void setSwitchPid(boolean switchPid) {
			this.switchPid = switchPid;
		}

	}

}
