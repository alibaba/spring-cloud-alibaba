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
import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.alibaba.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import org.springframework.core.Ordered;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

/**
 * @author xiaojing
 * @author hengyunabc
 * @author jiashuai.xie
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = SentinelConstants.PROPERTY_PREFIX)
public class SentinelProperties {

	/**
	 * earlier initialize heart-beat when the spring container starts <note> when the
	 * transport dependency is on classpath ,the configuration is effective </note>
	 */
	private boolean eager = false;

	/**
	 * enable sentinel auto configure, the default value is true
	 */
	private boolean enabled = true;

	/**
	 * configurations about datasource, like 'nacos', 'apollo', 'file', 'zookeeper'
	 */
	private Map<String, DataSourcePropertiesConfiguration> datasource = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);

	/**
	 * transport configuration about dashboard and client
	 */
	@NestedConfigurationProperty
	private Transport transport = new Transport();

	/**
	 * metric configuration about resource
	 */
	@NestedConfigurationProperty
	private Metric metric = new Metric();

	/**
	 * web servlet configuration <note> when the application is web ,the configuration is
	 * effective </note>
	 */
	@NestedConfigurationProperty
	private Servlet servlet = new Servlet();

	/**
	 * sentinel filter <note> when the application is web ,the configuration is effective
	 * </note>
	 */
	@NestedConfigurationProperty
	private Filter filter = new Filter();

	/**
	 * flow configuration
	 */
	@NestedConfigurationProperty
	private Flow flow = new Flow();

	/**
	 * sentinel log configuration {@link LogBase}
	 */
	@NestedConfigurationProperty
	private Log log = new Log();

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

	public static class Flow {

		/**
		 * the cold factor {@link SentinelConfig#COLD_FACTOR}
		 */
		private String coldFactor = "3";

		public String getColdFactor() {
			return coldFactor;
		}

		public void setColdFactor(String coldFactor) {
			this.coldFactor = coldFactor;
		}

	}

	public static class Servlet {

		/**
		 * The process page when the flow control is triggered
		 */
		private String blockPage;

		public String getBlockPage() {
			return blockPage;
		}

		public void setBlockPage(String blockPage) {
			this.blockPage = blockPage;
		}
	}

	public static class Metric {

		/**
		 * the metric file size {@link SentinelConfig#SINGLE_METRIC_FILE_SIZE}
		 */
		private String fileSingleSize;

		/**
		 * the total metric file count {@link SentinelConfig#TOTAL_METRIC_FILE_COUNT}
		 */
		private String fileTotalCount;

		/**
		 * charset when sentinel write or search metric file
		 * {@link SentinelConfig#CHARSET}
		 */
		private String charset = "UTF-8";

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
		 * sentinel api port,default value is 8721 {@link TransportConfig#SERVER_PORT}
		 */
		private String port = "8721";

		/**
		 * sentinel dashboard address, won't try to connect dashboard when address is
		 * empty {@link TransportConfig#CONSOLE_SERVER}
		 */
		private String dashboard = "";

		/**
		 * send heartbeat interval millisecond
		 * {@link TransportConfig#HEARTBEAT_INTERVAL_MS}
		 */
		private String heartbeatIntervalMs;

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

	}

	public static class Filter {

		/**
		 * sentinel filter chain order.
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

	public static class Log {

		/**
		 * sentinel log base dir
		 */
		private String dir;

		/**
		 * distinguish the log file by pid number
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
