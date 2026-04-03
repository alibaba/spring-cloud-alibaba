/*
 * Copyright 2024-present the original author or authors.
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
import org.jspecify.annotations.Nullable;
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
	private @Nullable String domainName;

	/**
	 * groupId.
	 */
	private @Nullable String groupId;

	/**
	 * host.
	 */
	private @Nullable String host;

	/**
	 * client port.
	 */
	private int port = 0;

	private @Nullable String enableUnits;

	private @Nullable String disableUnits;

	private @Nullable String enableSites;

	private @Nullable String disableSites;

	private boolean enableBatchWork;

	/**
	 * enabled: true; false.
	 */
	private boolean enabled = true;

	/**
	 * appName.
	 */
	private @Nullable String appName;

	/**
	 * appKey.
	 */
	private @Nullable String appKey;

	/**
	 * aliyunRamRole.
	 */
	private @Nullable String aliyunRamRole;

	/**
	 * aliyunAccessKey.
	 */
	private @Nullable String aliyunAccessKey;
	/**
	 * aliyunSecretKey.
	 */
	private @Nullable String aliyunSecretKey;

	/**
	 * STS ak.
	 */
	private @Nullable String stsAccessKey;

	/**
	 * STS sk.
	 */
	private @Nullable String stsSecretKey;

	/**
	 * STS secret token.
	 */
	private @Nullable String stsToken;

	/**
	 * Namespace UID.
	 */
	private @Nullable String namespace;

	/**
	 * endpoint.
	 */
	private @Nullable String endpoint;

	/**
	 * endpointPort.
	 */
	private @Nullable String endpointPort;

	/**
	 * namespaceName.
	 */
	private @Nullable String namespaceName;

	/**
	 * namespaceSource.
	 */
	private @Nullable String namespaceSource;

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
	private @Nullable String threadPoolMode;

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
	private @Nullable String label;

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
	private @Nullable String graceShutdownMode;

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

	private @Nullable Integer mapMasterRouterStrategy;

	private @Nullable String regionId;

	/**
	 * h2DatabaseUser.
	 */
	private @Nullable String h2DatabaseUser;

	/**
	 * h2DatabasePassword.
	 */
	private @Nullable String h2DatabasePassword;

	/**
	 * httpServerEnable.
	 */
	private @Nullable Boolean httpServerEnable;

	/**
	 * httpServerPort.
	 */
	private @Nullable Integer httpServerPort;

	/**
	 * maxMapDiskPercent.
	 */
	private @Nullable Float maxMapDiskPercent;

	private Map<String, JobProperty> jobs = new LinkedHashMap<>();

	private @Nullable String alarmChannel;

	private Map<String, ContactInfo> alarmUsers = new LinkedHashMap<>();

	private Map<String, Integer> processorPoolSize = new HashMap<>();

	public @Nullable String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public @Nullable String getGroupId() {
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

	public @Nullable String getHost() {
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

	public @Nullable String getEnableUnits() {
		return enableUnits;
	}

	public void setEnableUnits(String enableUnits) {
		this.enableUnits = enableUnits;
	}

	public @Nullable String getDisableUnits() {
		return disableUnits;
	}

	public void setDisableUnits(String disableUnits) {
		this.disableUnits = disableUnits;
	}

	public @Nullable String getEnableSites() {
		return enableSites;
	}

	public void setEnableSites(String enableSites) {
		this.enableSites = enableSites;
	}

	public @Nullable String getDisableSites() {
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

	public @Nullable String getAliyunAccessKey() {
		return aliyunAccessKey;
	}

	public void setAliyunAccessKey(String aliyunAccessKey) {
		this.aliyunAccessKey = aliyunAccessKey;
	}

	public @Nullable String getAliyunSecretKey() {
		return aliyunSecretKey;
	}

	public void setAliyunSecretKey(String aliyunSecretKey) {
		this.aliyunSecretKey = aliyunSecretKey;
	}

	public @Nullable String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public @Nullable String getEndpointPort() {
		return endpointPort;
	}

	public void setEndpointPort(String endpointPort) {
		this.endpointPort = endpointPort;
	}

	public @Nullable String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public @Nullable String getNamespaceSource() {
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

	public @Nullable String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public @Nullable String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public @Nullable String getStsAccessKey() {
		return stsAccessKey;
	}

	public void setStsAccessKey(String stsAccessKey) {
		this.stsAccessKey = stsAccessKey;
	}

	public @Nullable String getStsSecretKey() {
		return stsSecretKey;
	}

	public void setStsSecretKey(String stsSecretKey) {
		this.stsSecretKey = stsSecretKey;
	}

	public @Nullable String getStsToken() {
		return stsToken;
	}

	public @Nullable String getAliyunRamRole() {
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

	public @Nullable String getLabel() {
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

	public @Nullable String getGraceShutdownMode() {
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

	public @Nullable String getRegionId() {
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

	public @Nullable String getAlarmChannel() {
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

	public @Nullable String getThreadPoolMode() {
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

	public @Nullable Integer getMapMasterRouterStrategy() {
		return mapMasterRouterStrategy;
	}

	public void setMapMasterRouterStrategy(Integer mapMasterRouterStrategy) {
		this.mapMasterRouterStrategy = mapMasterRouterStrategy;
	}

	public @Nullable String getH2DatabaseUser() {
		return h2DatabaseUser;
	}

	public void setH2DatabaseUser(String h2DatabaseUser) {
		this.h2DatabaseUser = h2DatabaseUser;
	}

	public @Nullable String getH2DatabasePassword() {
		return h2DatabasePassword;
	}

	public void setH2DatabasePassword(String h2DatabasePassword) {
		this.h2DatabasePassword = h2DatabasePassword;
	}

	public @Nullable Boolean getHttpServerEnable() {
		return httpServerEnable;
	}

	public void setHttpServerEnable(Boolean httpServerEnable) {
		this.httpServerEnable = httpServerEnable;
	}

	public @Nullable Integer getHttpServerPort() {
		return httpServerPort;
	}

	public void setHttpServerPort(Integer httpServerPort) {
		this.httpServerPort = httpServerPort;
	}

	public @Nullable Float getMaxMapDiskPercent() {
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
