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
@ConfigurationProperties(prefix = SentinelConstants.PROPERTY_PREFIX)
public class SentinelProperties {

    /**
     * 是否提前初始化心跳连接
     */
    private boolean eager = false;

    /**
     * Enable sentinel auto configure, the default value is true
     */
    private boolean enabled = true;

    /**
     * 字符编码集
     */
    private String charset = "UTF-8";

    /**
     * 通信相关配置
     */
    @NestedConfigurationProperty
    private Transport transport = new Transport();

    /**
     * 监控数据相关配置
     */
    @NestedConfigurationProperty
    private Metric metric = new Metric();

    /**
     * web 相关配置
     */
    @NestedConfigurationProperty
    private Servlet servlet = new Servlet();

    /**
     * 限流相关
     */
    @NestedConfigurationProperty
    private Filter filter = new Filter();

    @NestedConfigurationProperty
    private Flow flow = new Flow();

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

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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

    public static class Flow {

        /**
         * 限流冷启动因子
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
         * url 限流后的处理页面
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
         * 监控数据写磁盘时单个文件的大小
         */
        private String fileSingleSize;

        /**
         * 监控数据在磁盘上的总数量
         */
        private String fileTotalCount;

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
    }

    public static class Transport {

        /**
         * sentinel api port,default value is 8721
         */
        private String port = "8721";

        /**
         * Sentinel dashboard address, won't try to connect dashboard when address is
         * empty
         */
        private String dashboard = "";

        /**
         * 客户端和DashBord心跳发送时间
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
