/*
 * Copyright 2024-2025 the original author or authors.
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

package com.alibaba.cloud.scheduling.schedulerx;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.cloud.scheduling.SchedulingConstants;
import com.alibaba.schedulerx.common.domain.ContactInfo;
import com.alibaba.schedulerx.common.util.JsonUtil;
import com.alibaba.schedulerx.worker.domain.WorkerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * schedulerx worker properties.
 *
 * @author yaohui
 **/
@ConfigurationProperties(prefix = SchedulerxProperties.CONFIG_PREFIX)
public class SchedulerxProperties implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerxProperties.class);

	/**
	 * schedulerx config prefix.
	 */
	public static final String CONFIG_PREFIX = SchedulingConstants.SCHEDULING_CONFIG_PREFIX + ".schedulerx";

	/**
	 * domainName.
	 */
	private String domainName;

	/**
	 * groupId.
	 */
	private String groupId;

	/**
	 * host.
	 */
	private String host;

	/**
	 * client port.
	 */
	private int port = 0;

	private String enableUnits;

	private String disableUnits;

	private String enableSites;

	private String disableSites;

	private boolean enableBatchWork;

	/**
	 * enabled: true; false.
	 */
	private boolean enabled = true;

	/**
	 * appName.
	 */
	private String appName;

	/**
	 * appKey.
	 */
	private String appKey;

	/**
	 * aliyunRamRole.
	 */
	private String aliyunRamRole;

	/**
	 * aliyunAccessKey.
	 */
	private String aliyunAccessKey;
	/**
	 * aliyunSecretKey.
	 */
	private String aliyunSecretKey;

	/**
	 * STS ak.
	 */
	private String stsAccessKey;

	/**
	 * STS sk.
	 */
	private String stsSecretKey;

	/**
	 * STS secret token.
	 */
	private String stsToken;

	/**
	 * Namespace UID.
	 */
	private String namespace;

	/**
	 * endpoint.
	 */
	private String endpoint;

	/**
	 * endpointPort.
	 */
	private String endpointPort;

	/**
	 * namespaceName.
	 */
	private String namespaceName;

	/**
	 * namespaceSource.
	 */
	private String namespaceSource;

	/**
	 * maxTaskBodySize (byte).
	 */
	private int maxTaskBodySize = WorkerConstants.TASK_BODY_SIZE_MAX_DEFAULT;

	private boolean blockAppStart = true;

	/**
	 * slsCollectorEnable.
	 */
	private boolean slsCollectorEnable = true;

	/**
	 * shareContainerPool.
	 */
	private boolean shareContainerPool = false;

	/**
	 * threadPoolMode.
	 */
	private String threadPoolMode;

	/**
	 * sharePoolSize.
	 */
	private int sharePoolSize = WorkerConstants.SHARE_POOL_SIZE_DEFAULT;

	/**
	 * sharePoolQueueSize.
	 */
	private int sharePoolQueueSize = Integer.MAX_VALUE;

	/**
	 * Wlabel.
	 */
	private String label;

	private String labelPath = "/etc/podinfo/labels";

	/**
	 * enableCgroupMetrics.
	 */
	private boolean enableCgroupMetrics = false;

	/**
	 * cgroupPathPrefix.
	 */
	private String cgroupPathPrefix = "/sys/fs/cgroup/cpu/";

	/**
	 * akkaRemotingAutoRecover.
	 */
	private boolean akkaRemotingAutoRecover = true;

	/**
	 * enableHeartbeatLog.
	 */
	private boolean enableHeartbeatLog = true;

	/**
	 * mapMasterStatusCheckInterval(ms).
	 */
	private int mapMasterStatusCheckInterval = WorkerConstants.Map_MASTER_STATUS_CHECK_INTERVAL_DEFAULT;

	/**
	 * enableSecondDelayCycleIntervalMs.
	 */
	private boolean enableSecondDelayCycleIntervalMs = false;

	/**
	 * enableMapMasterFailover.
	 */
	private boolean enableMapMasterFailover = true;

	/**
	 * enableSecondDelayStandaloneDispatch.
	 */
	private boolean enableSecondDelayStandaloneDispatch = false;

	/**
	 * pageSize.
	 */
	private int pageSize = 1000;

	/**
	 * GraceShutdownMode(WAIT_ALL; WAIT_RUNNING;).
	 */
	private String graceShutdownMode;

	/**
	 * graceShutdownTimeout.
	 */
	private long graceShutdownTimeout = WorkerConstants.GRACE_SHUTDOWN_TIMEOUT_DEFAULT;

	/**
	 * broadcastDispatchThreadNum.
	 */
	private int broadcastDispatchThreadNum = 4;

	/**
	 * broadcastDispatchRetryTimes.
	 */
	private int broadcastDispatchRetryTimes = 1;

	/**
	 * broadcastDispatchThreadEnable.
	 */
	private boolean broadcastDispatchThreadEnable = false;

	/**
	 * broadcastMasterExecEnable.
	 */
	private boolean broadcastMasterExecEnable = true;

	/**
	 * mapMasterDispatchRandom.
	 */
	private boolean mapMasterDispatchRandom = false;

	private Integer mapMasterRouterStrategy;

	private String regionId;

	/**
	 * h2DatabaseUser.
	 */
	private String h2DatabaseUser;

	/**
	 * h2DatabasePassword.
	 */
	private String h2DatabasePassword;

	/**
	 * httpServerEnable.
	 */
	private Boolean httpServerEnable;

	/**
	 * httpServerPort.
	 */
	private Integer httpServerPort;

	/**
	 * maxMapDiskPercent.
	 */
	private Float maxMapDiskPercent;

	private Map<String, JobProperty> jobs = new LinkedHashMap<>();

	private String alarmChannel;

	private Map<String, ContactInfo> alarmUsers = new LinkedHashMap<>();

	private Map<String, Integer> processorPoolSize = new HashMap<>();

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getEnableUnits() {
		return enableUnits;
	}

	public void setEnableUnits(String enableUnits) {
		this.enableUnits = enableUnits;
	}

	public String getDisableUnits() {
		return disableUnits;
	}

	public void setDisableUnits(String disableUnits) {
		this.disableUnits = disableUnits;
	}

	public String getEnableSites() {
		return enableSites;
	}

	public void setEnableSites(String enableSites) {
		this.enableSites = enableSites;
	}

	public String getDisableSites() {
		return disableSites;
	}

	public void setDisableSites(String disableSites) {
		this.disableSites = disableSites;
	}

	public boolean isEnableBatchWork() {
		return enableBatchWork;
	}

	public void setEnableBatchWork(boolean enableBatchWork) {
		this.enableBatchWork = enableBatchWork;
	}

	public String getAliyunAccessKey() {
		return aliyunAccessKey;
	}

	public void setAliyunAccessKey(String aliyunAccessKey) {
		this.aliyunAccessKey = aliyunAccessKey;
	}

	public String getAliyunSecretKey() {
		return aliyunSecretKey;
	}

	public void setAliyunSecretKey(String aliyunSecretKey) {
		this.aliyunSecretKey = aliyunSecretKey;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpointPort() {
		return endpointPort;
	}

	public void setEndpointPort(String endpointPort) {
		this.endpointPort = endpointPort;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public String getNamespaceSource() {
		return namespaceSource;
	}

	public void setNamespaceSource(String namespaceSource) {
		this.namespaceSource = namespaceSource;
	}

	public int getMaxTaskBodySize() {
		return maxTaskBodySize;
	}

	public void setMaxTaskBodySize(int maxTaskBodySize) {
		this.maxTaskBodySize = maxTaskBodySize;
	}

	public boolean isBlockAppStart() {
		return blockAppStart;
	}

	public void setBlockAppStart(boolean blockAppStart) {
		this.blockAppStart = blockAppStart;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getStsAccessKey() {
		return stsAccessKey;
	}

	public void setStsAccessKey(String stsAccessKey) {
		this.stsAccessKey = stsAccessKey;
	}

	public String getStsSecretKey() {
		return stsSecretKey;
	}

	public void setStsSecretKey(String stsSecretKey) {
		this.stsSecretKey = stsSecretKey;
	}

	public String getStsToken() {
		return stsToken;
	}

	public String getAliyunRamRole() {
		return aliyunRamRole;
	}

	public void setAliyunRamRole(String aliyunRamRole) {
		this.aliyunRamRole = aliyunRamRole;
	}

	public void setStsToken(String stsToken) {
		this.stsToken = stsToken;
	}

	public boolean isSlsCollectorEnable() {
		return slsCollectorEnable;
	}

	public void setSlsCollectorEnable(boolean slsCollectorEnable) {
		this.slsCollectorEnable = slsCollectorEnable;
	}

	public boolean isShareContainerPool() {
		return shareContainerPool;
	}

	public void setShareContainerPool(boolean shareContainerPool) {
		this.shareContainerPool = shareContainerPool;
	}

	public int getSharePoolSize() {
		return sharePoolSize;
	}

	public void setSharePoolSize(int sharePoolSize) {
		this.sharePoolSize = sharePoolSize;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		if (label != null) {
			if (label.startsWith("#") && label.endsWith("#")) {
				String labelKey = label.substring(1, label.length() - 1);
				this.label = System.getenv(labelKey);
				return;
			}
		}
		this.label = label;
	}

	public String getLabelPath() {
		return labelPath;
	}

	public void setLabelPath(String labelPath) {
		this.labelPath = labelPath;
	}

	public boolean isEnableCgroupMetrics() {
		return enableCgroupMetrics;
	}

	public void setEnableCgroupMetrics(boolean enableCgroupMetrics) {
		this.enableCgroupMetrics = enableCgroupMetrics;
	}

	public String getCgroupPathPrefix() {
		return cgroupPathPrefix;
	}

	public void setCgroupPathPrefix(String cgroupPathPrefix) {
		this.cgroupPathPrefix = cgroupPathPrefix;
	}

	public boolean isAkkaRemotingAutoRecover() {
		return akkaRemotingAutoRecover;
	}

	public void setAkkaRemotingAutoRecover(boolean akkaRemotingAutoRecover) {
		this.akkaRemotingAutoRecover = akkaRemotingAutoRecover;
	}

	public boolean isEnableHeartbeatLog() {
		return enableHeartbeatLog;
	}

	public void setEnableHeartbeatLog(boolean enableHeartbeatLog) {
		this.enableHeartbeatLog = enableHeartbeatLog;
	}

	public int getMapMasterStatusCheckInterval() {
		return mapMasterStatusCheckInterval;
	}

	public void setMapMasterStatusCheckInterval(int mapMasterStatusCheckInterval) {
		this.mapMasterStatusCheckInterval = mapMasterStatusCheckInterval;
	}

	public boolean isEnableSecondDelayCycleIntervalMs() {
		return enableSecondDelayCycleIntervalMs;
	}

	public void setEnableSecondDelayCycleIntervalMs(boolean enableSecondDelayCycleIntervalMs) {
		this.enableSecondDelayCycleIntervalMs = enableSecondDelayCycleIntervalMs;
	}

	public boolean isEnableMapMasterFailover() {
		return enableMapMasterFailover;
	}

	public void setEnableMapMasterFailover(boolean enableMapMasterFailover) {
		this.enableMapMasterFailover = enableMapMasterFailover;
	}

	public boolean isEnableSecondDelayStandaloneDispatch() {
		return enableSecondDelayStandaloneDispatch;
	}

	public void setEnableSecondDelayStandaloneDispatch(boolean enableSecondDelayStandaloneDispatch) {
		this.enableSecondDelayStandaloneDispatch = enableSecondDelayStandaloneDispatch;
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getGraceShutdownMode() {
		return graceShutdownMode;
	}

	public void setGraceShutdownMode(String graceShutdownMode) {
		this.graceShutdownMode = graceShutdownMode;
	}

	public long getGraceShutdownTimeout() {
		return graceShutdownTimeout;
	}

	public void setGraceShutdownTimeout(long graceShutdownTimeout) {
		this.graceShutdownTimeout = graceShutdownTimeout;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public Map<String, JobProperty> getJobs() {
		return jobs;
	}

	public void setJobs(Map<String, JobProperty> jobs) {
		this.jobs = jobs;
	}

	public String getAlarmChannel() {
		return alarmChannel;
	}

	public void setAlarmChannel(String alarmChannel) {
		this.alarmChannel = alarmChannel;
	}

	public Map<String, ContactInfo> getAlarmUsers() {
		return alarmUsers;
	}

	public void setAlarmUsers(Map<String, ContactInfo> alarmUsers) {
		this.alarmUsers = alarmUsers;
	}

	public int getBroadcastDispatchThreadNum() {
		return broadcastDispatchThreadNum;
	}

	public void setBroadcastDispatchThreadNum(int broadcastDispatchThreadNum) {
		this.broadcastDispatchThreadNum = broadcastDispatchThreadNum;
	}

	public boolean isBroadcastDispatchThreadEnable() {
		return broadcastDispatchThreadEnable;
	}

	public void setBroadcastDispatchThreadEnable(boolean broadcastDispatchThreadEnable) {
		this.broadcastDispatchThreadEnable = broadcastDispatchThreadEnable;
	}

	public String getThreadPoolMode() {
		return threadPoolMode;
	}

	public void setThreadPoolMode(String threadPoolMode) {
		this.threadPoolMode = threadPoolMode;
	}

	public Map<String, Integer> getProcessorPoolSize() {
		return processorPoolSize;
	}

	public void setProcessorPoolSize(Map<String, Integer> processorPoolSize) {
		this.processorPoolSize = processorPoolSize;
	}

	public int getSharePoolQueueSize() {
		return sharePoolQueueSize;
	}

	public void setSharePoolQueueSize(int sharePoolQueueSize) {
		this.sharePoolQueueSize = sharePoolQueueSize;
	}

	public boolean isMapMasterDispatchRandom() {
		return mapMasterDispatchRandom;
	}

	public void setMapMasterDispatchRandom(boolean mapMasterDispatchRandom) {
		this.mapMasterDispatchRandom = mapMasterDispatchRandom;
	}

	public boolean isBroadcastMasterExecEnable() {
		return broadcastMasterExecEnable;
	}

	public void setBroadcastMasterExecEnable(boolean broadcastMasterExecEnable) {
		this.broadcastMasterExecEnable = broadcastMasterExecEnable;
	}

	public int getBroadcastDispatchRetryTimes() {
		return broadcastDispatchRetryTimes;
	}

	public void setBroadcastDispatchRetryTimes(int broadcastDispatchRetryTimes) {
		this.broadcastDispatchRetryTimes = broadcastDispatchRetryTimes;
	}

	public Integer getMapMasterRouterStrategy() {
		return mapMasterRouterStrategy;
	}

	public void setMapMasterRouterStrategy(Integer mapMasterRouterStrategy) {
		this.mapMasterRouterStrategy = mapMasterRouterStrategy;
	}

	public String getH2DatabaseUser() {
		return h2DatabaseUser;
	}

	public void setH2DatabaseUser(String h2DatabaseUser) {
		this.h2DatabaseUser = h2DatabaseUser;
	}

	public String getH2DatabasePassword() {
		return h2DatabasePassword;
	}

	public void setH2DatabasePassword(String h2DatabasePassword) {
		this.h2DatabasePassword = h2DatabasePassword;
	}

	public Boolean getHttpServerEnable() {
		return httpServerEnable;
	}

	public void setHttpServerEnable(Boolean httpServerEnable) {
		this.httpServerEnable = httpServerEnable;
	}

	public Integer getHttpServerPort() {
		return httpServerPort;
	}

	public void setHttpServerPort(Integer httpServerPort) {
		this.httpServerPort = httpServerPort;
	}

	public Float getMaxMapDiskPercent() {
		return maxMapDiskPercent;
	}

	public void setMaxMapDiskPercent(float maxMapDiskPercent) {
		this.maxMapDiskPercent = maxMapDiskPercent;
	}

	@Override
	public void afterPropertiesSet() {
		logger.info("SchedulerxProperties->" + JsonUtil.toJson(this));
	}
}
