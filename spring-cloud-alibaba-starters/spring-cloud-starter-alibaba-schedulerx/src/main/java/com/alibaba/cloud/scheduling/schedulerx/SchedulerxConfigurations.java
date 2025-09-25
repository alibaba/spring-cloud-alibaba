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

import com.alibaba.cloud.scheduling.schedulerx.service.JobSyncService;
import com.alibaba.cloud.scheduling.schedulerx.service.ScheduledJobSyncConfigurer;
import com.alibaba.schedulerx.common.util.ConfigUtil;
import com.alibaba.schedulerx.common.util.StringUtils;
import com.alibaba.schedulerx.worker.SchedulerxWorker;
import com.alibaba.schedulerx.worker.domain.WorkerConstants;
import com.alibaba.schedulerx.worker.log.LogFactory;
import com.alibaba.schedulerx.worker.log.Logger;
import com.alibaba.schedulerx.worker.processor.springscheduling.NoOpScheduler;
import com.alibaba.schedulerx.worker.processor.springscheduling.SchedulerxAnnotationBeanPostProcessor;
import com.alibaba.schedulerx.worker.processor.springscheduling.SchedulerxSchedulingConfigurer;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

/**
 * @author yaohui
 **/
public class SchedulerxConfigurations {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(SchedulerxWorker.class)
	@ConditionalOnProperty(prefix = SchedulerxProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
	static class SchedulerxWorkerConfiguration {

		private static final Logger LOGGER = LogFactory.getLogger(SchedulerxAutoConfigure.class);

		private static final String WORKER_STARTER_SPRING_CLOUD = "springcloud";

		@Autowired
		private SchedulerxProperties properties;

		@Bean
		public JobSyncService jobSyncService() {
			return new JobSyncService();
		}

		@PostConstruct
		public void syncJobs() throws Exception {
			if (!properties.getJobs().isEmpty()) {
				LOGGER.info("{}.jobs is not empty, start to sync jobs...", SchedulerxProperties.CONFIG_PREFIX);
				jobSyncService().syncJobs();
				LOGGER.info("sync jobs finished.");
			}
		}

		@Bean
		public SchedulerxWorker schedulerxWorker() {
			SchedulerxWorker schedulerxWorker = new SchedulerxWorker();
			schedulerxWorker.setDomainName(properties.getDomainName());
			schedulerxWorker.setGroupId(properties.getGroupId());
			schedulerxWorker.setEnableBatchWork(properties.isEnableBatchWork());
			schedulerxWorker.setDisableSites(properties.getDisableSites());
			schedulerxWorker.setEnableSites(properties.getEnableSites());
			schedulerxWorker.setDisableUnits(properties.getDisableUnits());
			schedulerxWorker.setEnableUnits(properties.getEnableUnits());
			schedulerxWorker.setAppKey(properties.getAppKey());
			schedulerxWorker.setAliyunAccessKey(properties.getAliyunAccessKey());
			schedulerxWorker.setAliyunSecretKey(properties.getAliyunSecretKey());
			schedulerxWorker.setNamespace(properties.getNamespace());
			schedulerxWorker.setHost(properties.getHost());
			schedulerxWorker.setPort(properties.getPort());
			schedulerxWorker.setEndpoint(properties.getEndpoint());
			schedulerxWorker.setNamespaceSource(properties.getNamespaceSource());
			schedulerxWorker.setMaxTaskBodySize(properties.getMaxTaskBodySize());
			schedulerxWorker.setBlockAppStart(properties.isBlockAppStart());
			schedulerxWorker.setSTSAccessKey(properties.getStsAccessKey());
			schedulerxWorker.setSTSSecretKey(properties.getStsSecretKey());
			schedulerxWorker.setSTSSecretToken(properties.getStsToken());
			schedulerxWorker.setSlsCollectorEnable(properties.isSlsCollectorEnable());
			schedulerxWorker.setShareContainerPool(properties.isShareContainerPool());
			schedulerxWorker.setThreadPoolMode(properties.getThreadPoolMode());
			schedulerxWorker.setLabel(properties.getLabel());
			schedulerxWorker.setLabelPath(properties.getLabelPath());
			if (properties.isShareContainerPool() || WorkerConstants.THREAD_POOL_MODE_ALL.equals(properties.getThreadPoolMode())) {
				schedulerxWorker.setSharePoolSize(properties.getSharePoolSize());
				schedulerxWorker.setSharePoolQueueSize(properties.getSharePoolQueueSize());
			}
			if (StringUtils.isNotEmpty(properties.getEndpointPort())) {
				schedulerxWorker.setEndpointPort(Integer.parseInt(properties.getEndpointPort()));
			}
			schedulerxWorker.setEnableCgroupMetrics(properties.isEnableCgroupMetrics());
			if (properties.isEnableCgroupMetrics()) {
				schedulerxWorker.setCgroupPathPrefix(properties.getCgroupPathPrefix());
			}
			if (StringUtils.isNotEmpty(properties.getNamespaceSource())) {
				schedulerxWorker.setNamespaceSource(properties.getNamespaceSource());
			}
			schedulerxWorker.setAkkaRemotingAutoRecover(properties.isAkkaRemotingAutoRecover());
			schedulerxWorker.setEnableHeartbeatLog(properties.isEnableHeartbeatLog());
			schedulerxWorker.setMapMasterStatusCheckInterval(properties.getMapMasterStatusCheckInterval());
			schedulerxWorker.setEnableSecondDelayCycleIntervalMs(properties.isEnableSecondDelayCycleIntervalMs());
			schedulerxWorker.setEnableMapMasterFailover(properties.isEnableMapMasterFailover());
			schedulerxWorker.setEnableSecondDelayStandaloneDispatch(properties.isEnableSecondDelayStandaloneDispatch());
			schedulerxWorker.setPageSize(properties.getPageSize());
			schedulerxWorker.setGraceShutdownMode(properties.getGraceShutdownMode());
			if (properties.getGraceShutdownTimeout() > 0) {
				schedulerxWorker.setGraceShutdownTimeout(properties.getGraceShutdownTimeout());
			}
			schedulerxWorker.setBroadcastDispatchThreadNum(properties.getBroadcastDispatchThreadNum());
			schedulerxWorker.setBroadcastDispatchThreadEnable(properties.isBroadcastDispatchThreadEnable());
			schedulerxWorker.setBroadcastMasterExecEnable(properties.isBroadcastMasterExecEnable());
			schedulerxWorker.setBroadcastDispatchRetryTimes(properties.getBroadcastDispatchRetryTimes());
			schedulerxWorker.setProcessorPoolSize(properties.getProcessorPoolSize());
			schedulerxWorker.setMapMasterDispatchRandom(properties.isMapMasterDispatchRandom());
			schedulerxWorker.setMapMasterRouterStrategy(properties.getMapMasterRouterStrategy());
			if (StringUtils.isNotEmpty(properties.getH2DatabaseUser())) {
				schedulerxWorker.setH2DatabaseUser(properties.getH2DatabaseUser());
			}
			if (StringUtils.isNotEmpty(properties.getH2DatabasePassword())) {
				schedulerxWorker.setH2DatabasePassword(properties.getH2DatabasePassword());
			}
			schedulerxWorker.setHttpServerEnable(properties.getHttpServerEnable());
			schedulerxWorker.setHttpServerPort(properties.getHttpServerPort());
			if (properties.getMaxMapDiskPercent() != null) {
				schedulerxWorker.setMaxMapDiskPercent(properties.getMaxMapDiskPercent());
			}

			ConfigUtil.getWorkerConfig().setProperty(WorkerConstants.WORKER_STARTER_MODE,
					WORKER_STARTER_SPRING_CLOUD);
			return schedulerxWorker;
		}
	}


	@Configuration(proxyBeanMethods = false)
	@AutoConfigureAfter(SchedulerxWorkerConfiguration.class)
	@ConditionalOnBean(SchedulerxWorker.class)
	static class SpringScheduleAdaptConfiguration {

		@Bean(ScheduledAnnotationBeanPostProcessor.DEFAULT_TASK_SCHEDULER_BEAN_NAME)
		public NoOpScheduler noOpScheduler() {
			return new NoOpScheduler();
		}

		@Bean
		public SchedulerxSchedulingConfigurer schedulerxSchedulingConfigurer() {
			return new SchedulerxSchedulingConfigurer(noOpScheduler(), true);
		}

		@Bean
		public static SchedulerxAnnotationBeanPostProcessor schedulerxAnnotationBeanPostProcessor() {
			return new SchedulerxAnnotationBeanPostProcessor();
		}

		@Bean
		@ConditionalOnProperty(prefix = SchedulerxProperties.CONFIG_PREFIX, name = "task-sync", havingValue = "true")
		public ScheduledJobSyncConfigurer scheduledJobSyncConfigurer() {
			return new ScheduledJobSyncConfigurer();
		}
	}
}
