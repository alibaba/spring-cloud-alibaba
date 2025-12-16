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

package com.alibaba.cloud.scheduling.schedulerx.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.scheduling.schedulerx.JobProperty;
import com.alibaba.cloud.scheduling.schedulerx.SchedulerxProperties;
import com.alibaba.cloud.scheduling.schedulerx.constants.SchedulerxConstants;
import com.alibaba.cloud.scheduling.schedulerx.util.CronExpression;
import com.alibaba.schedulerx.common.domain.ContactInfo;
import com.alibaba.schedulerx.common.domain.ExecuteMode;
import com.alibaba.schedulerx.common.domain.JobType;
import com.alibaba.schedulerx.common.domain.TimeType;
import com.alibaba.schedulerx.common.sdk.common.MonitorConfig;
import com.alibaba.schedulerx.common.util.JsonUtil;
import com.alibaba.schedulerx.common.util.StringUtils;
import com.alibaba.schedulerx.worker.log.LogFactory;
import com.alibaba.schedulerx.worker.log.Logger;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.InstanceProfileCredentialsProvider;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.schedulerx2.model.v20190430.CreateAppGroupRequest;
import com.aliyuncs.schedulerx2.model.v20190430.CreateAppGroupResponse;
import com.aliyuncs.schedulerx2.model.v20190430.CreateJobRequest;
import com.aliyuncs.schedulerx2.model.v20190430.CreateJobResponse;
import com.aliyuncs.schedulerx2.model.v20190430.CreateNamespaceRequest;
import com.aliyuncs.schedulerx2.model.v20190430.CreateNamespaceResponse;
import com.aliyuncs.schedulerx2.model.v20190430.GetJobInfoRequest;
import com.aliyuncs.schedulerx2.model.v20190430.GetJobInfoResponse;
import com.aliyuncs.schedulerx2.model.v20190430.GetJobInfoResponse.Data.JobConfigInfo;
import com.aliyuncs.schedulerx2.model.v20190430.UpdateJobRequest;
import com.aliyuncs.schedulerx2.model.v20190430.UpdateJobResponse;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * JobSyncService.
 *
 * @author xiaomeng.hxm
 */
public class JobSyncService {

	private static final Logger logger = LogFactory.getLogger(JobSyncService.class);

	@Autowired
	private SchedulerxProperties properties;

	private DefaultAcsClient client;

	private synchronized DefaultAcsClient getClient() {
		// build aliyun pop client
		if (client == null) {
			DefaultProfile.addEndpoint(properties.getRegionId(), SchedulerxConstants.ALIYUN_POP_PRODUCT, SchedulerxConstants.ALIYUN_POP_SCHEDULERX_ENDPOINT);
			if (StringUtils.isNotEmpty(properties.getAliyunRamRole())) {
				DefaultProfile profile = DefaultProfile.getProfile(properties.getRegionId());
				InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(
						properties.getAliyunRamRole());
				client = new DefaultAcsClient(profile, provider);
			}
			else {
				DefaultProfile defaultProfile = DefaultProfile.getProfile(properties.getRegionId(),
						properties.getAliyunAccessKey(),
						properties.getAliyunSecretKey());
				client = new DefaultAcsClient(defaultProfile);
			}
		}
		return client;
	}

	/**
	 * Sync job config.
	 *
	 * @param jobs            job configs
	 * @param namespaceSource namespace source
	 * @throws Exception sync job config exception
	 */
	public synchronized void syncJobs(Map<String, JobProperty> jobs, String namespaceSource) throws Exception {
		DefaultAcsClient client = getClient();
		for (Entry<String, JobProperty> entry : jobs.entrySet()) {
			String jobName = entry.getKey();
			JobProperty jobProperty = entry.getValue();
			JobConfigInfo jobConfigInfo = getJob(client, jobName, namespaceSource);
			if (jobConfigInfo == null) {
				createJob(client, jobName, jobProperty, namespaceSource);
			}
			else if (jobProperty.isOverwrite()) {
				updateJob(client, jobConfigInfo, jobProperty, namespaceSource);
			}
		}
	}

	/**
	 * sync jobs.
	 *
	 * @throws Exception sync jobs exception
	 */
	public void syncJobs() throws Exception {
		// 1. create namespace
		if (syncNamespace(getClient())) {
			// 2. create app group
			if (syncAppGroup(getClient())) {
				syncJobs(properties.getJobs(), getNamespaceSource());
				properties.setNamespaceSource(getNamespaceSource());
			}
		}
	}

	/**
	 * sync namespace.
	 *
	 * @param client pop client
	 * @return true if success
	 * @throws Exception sync namespace exception
	 */
	public boolean syncNamespace(DefaultAcsClient client) throws Exception {
		if (StringUtils.isEmpty(properties.getNamespace())) {
			logger.error("please set {}.namespace", SchedulerxProperties.CONFIG_PREFIX);
			throw new IOException(String.format("please set %s.namespace", SchedulerxProperties.CONFIG_PREFIX));
		}

		if (StringUtils.isEmpty(properties.getNamespaceName())) {
			logger.error("please set {}.namespaceName", SchedulerxProperties.CONFIG_PREFIX);
			throw new IOException(String.format("please set %s.namespaceName", SchedulerxProperties.CONFIG_PREFIX));
		}

		CreateNamespaceRequest request = new CreateNamespaceRequest();
		request.setUid(properties.getNamespace());
		request.setName(properties.getNamespaceName());
		request.setSource(getNamespaceSource());
		CreateNamespaceResponse response = client.getAcsResponse(request);
		if (response.getSuccess()) {
			logger.info(JsonUtil.toJson(response));
			return true;
		}
		else {
			throw new IOException(response.getMessage());
		}

	}

	/**
	 * sync app group.
	 *
	 * @param client pop client
	 * @return sync app group result
	 * @throws IOException     sync app group exception.
	 * @throws ClientException sync app group pop client exception.
	 */
	public boolean syncAppGroup(DefaultAcsClient client) throws IOException, ClientException {
		if (StringUtils.isEmpty(properties.getAppName())) {
			logger.error("please set {}.appName", SchedulerxProperties.CONFIG_PREFIX);
			throw new IOException(String.format("please set %s.appName", SchedulerxProperties.CONFIG_PREFIX));
		}

		if (StringUtils.isEmpty(properties.getAppKey())) {
			logger.error("please set {}.appKey", SchedulerxProperties.CONFIG_PREFIX);
			throw new IOException(String.format("please set %s.appKey", SchedulerxProperties.CONFIG_PREFIX));
		}

		if (StringUtils.isEmpty(properties.getGroupId())) {
			logger.error("please set {}.groupId", SchedulerxProperties.CONFIG_PREFIX);
			throw new IOException(String.format("please set %s.groupId", SchedulerxProperties.CONFIG_PREFIX));
		}

		CreateAppGroupRequest request = new CreateAppGroupRequest();
		request.setNamespace(properties.getNamespace());
		request.setNamespaceSource(getNamespaceSource());
		request.setAppName(properties.getAppName());
		request.setGroupId(properties.getGroupId());
		request.setAppKey(properties.getAppKey());
		if (StringUtils.isNotEmpty(properties.getAlarmChannel())) {
			MonitorConfig monitorConfig = new MonitorConfig();
			monitorConfig.setSendChannel(properties.getAlarmChannel());
			request.setMonitorConfigJson(JsonUtil.toJson(monitorConfig));
		}
		if (!properties.getAlarmUsers().isEmpty()) {
			List<ContactInfo> contactInfos = new ArrayList(properties.getAlarmUsers().values());
			request.setMonitorContactsJson(JsonUtil.toJson(contactInfos));
		}
		CreateAppGroupResponse response = client.getAcsResponse(request);
		if (response.getSuccess()) {
			logger.info(JsonUtil.toJson(response));
			return true;
		}
		else {
			throw new IOException(response.getMessage());
		}
	}

	/**
	 * Get job config info.
	 *
	 * @param client          pop client
	 * @param jobName         job name
	 * @param namespaceSource namespace source
	 * @return job config info.
	 * @throws Exception get job config info exception.
	 */
	private JobConfigInfo getJob(DefaultAcsClient client, String jobName, String namespaceSource) throws Exception {
		GetJobInfoRequest request = new GetJobInfoRequest();
		request.setNamespace(properties.getNamespace());
		request.setNamespaceSource(namespaceSource);
		request.setGroupId(properties.getGroupId());
		request.setJobId(0L);
		request.setJobName(jobName);
		GetJobInfoResponse response = client.getAcsResponse(request);
		if (response.getSuccess()) {
			return response.getData().getJobConfigInfo();
		}
		return null;
	}

	/**
	 * create job.
	 *
	 * @param client          pop client
	 * @param jobName         job name
	 * @param jobProperty     job property
	 * @param namespaceSource namespace source
	 * @throws Exception create job exception
	 */
	private void createJob(DefaultAcsClient client, String jobName, JobProperty jobProperty, String namespaceSource) throws Exception {
		CreateJobRequest request = new CreateJobRequest();
		request.setNamespace(properties.getNamespace());
		request.setNamespaceSource(namespaceSource);
		request.setGroupId(properties.getGroupId());
		request.setName(jobName);
		request.setParameters(jobProperty.getJobParameter());

		if (jobProperty.getJobType().equals(JobType.JAVA.getKey())) {
			request.setJobType(JobType.JAVA.getKey());
			request.setClassName(jobProperty.getClassName());
		}
		else {
			request.setJobType(jobProperty.getJobType());
		}

		if (SchedulerxConstants.JOB_MODEL_MAPREDUCE_ALIAS.equals(jobProperty.getJobModel())) {
			request.setExecuteMode(ExecuteMode.BATCH.getKey());
		}
		else {
			request.setExecuteMode(jobProperty.getJobModel());
		}
		if (StringUtils.isNotEmpty(jobProperty.getDescription())) {
			request.setDescription(jobProperty.getDescription());
		}

		if (StringUtils.isNotEmpty(jobProperty.getContent())) {
			request.setContent(jobProperty.getContent());
		}

		if (StringUtils.isNotEmpty(jobProperty.getCron()) && StringUtils.isNotEmpty(jobProperty.getOneTime())) {
			throw new IOException("cron and oneTime shouldn't set together");
		}
		if (StringUtils.isNotEmpty(jobProperty.getCron())) {
			CronExpression cronExpression = new CronExpression(jobProperty.getCron());
			Date now = new Date();
			Date nextData = cronExpression.getTimeAfter(now);
			Date next2Data = cronExpression.getTimeAfter(nextData);
			if (nextData != null && next2Data != null) {
				long interval = TimeUnit.MILLISECONDS.toSeconds((next2Data.getTime() - nextData.getTime()));
				if (interval < SchedulerxConstants.SECOND_DELAY_MAX_VALUE) {
					request.setTimeType(TimeType.SECOND_DELAY.getValue());
					request.setTimeExpression(String.valueOf(interval < SchedulerxConstants.SECOND_DELAY_MIN_VALUE ?
							SchedulerxConstants.SECOND_DELAY_MIN_VALUE : interval));
				}
				else {
					request.setTimeType(TimeType.CRON.getValue());
					request.setTimeExpression(jobProperty.getCron());
				}
			}
			else {
				request.setTimeType(TimeType.CRON.getValue());
				request.setTimeExpression(jobProperty.getCron());
			}
		}
		else if (StringUtils.isNotEmpty(jobProperty.getOneTime())) {
			request.setTimeType(TimeType.ONE_TIME.getValue());
			request.setTimeExpression(jobProperty.getOneTime());
		}
		else {
			request.setTimeType(TimeType.API.getValue());
		}

		if (jobProperty.getTimeType() != null) {
			request.setTimeType(jobProperty.getTimeType());
			if (StringUtils.isNotEmpty(jobProperty.getTimeExpression())) {
				request.setTimeExpression(jobProperty.getTimeExpression());
			}
		}

		request.setTimeoutEnable(true);
		request.setTimeoutKillEnable(true);
		request.setSendChannel(SchedulerxConstants.JOB_ALARM_CHANNEL_DEFAULT);
		request.setFailEnable(true);
		request.setTimeout(SchedulerxConstants.JOB_TIMEOUT_DEFAULT);
		request.setMaxAttempt(SchedulerxConstants.JOB_RETRY_COUNT_DEFAULT);
		request.setAttemptInterval(SchedulerxConstants.JOB_RETRY_INTERVAL_DEFAULT);
		CreateJobResponse response = client.getAcsResponse(request);
		if (response.getSuccess()) {
			logger.info("create schedulerx job successfully, jobId={}, jobName={}", response.getData().getJobId(), jobName);
		}
		else {
			throw new IOException("create schedulerx job failed, jobName=" + jobName + ", message=" + response.getMessage());
		}
	}

	/**
	 * update job.
	 *
	 * @param client          pop client
	 * @param jobConfigInfo   job config info
	 * @param jobProperty     job property
	 * @param namespaceSource namespace source
	 * @throws Exception update job exception
	 */
	private void updateJob(DefaultAcsClient client, JobConfigInfo jobConfigInfo, JobProperty jobProperty, String namespaceSource) throws Exception {
		String executeMode = jobProperty.getJobModel();
		if (SchedulerxConstants.JOB_MODEL_MAPREDUCE_ALIAS.equals(jobProperty.getJobModel())) {
			executeMode = ExecuteMode.BATCH.getKey();
		}
		int timeType;
		String timeExpression = null;
		if (StringUtils.isNotEmpty(jobProperty.getCron()) && StringUtils.isNotEmpty(jobProperty.getOneTime())) {
			throw new IOException("cron and oneTime shouldn't set together");
		}
		if (StringUtils.isNotEmpty(jobProperty.getCron())) {
			CronExpression cronExpression = new CronExpression(jobProperty.getCron());
			Date now = new Date();
			Date nextData = cronExpression.getTimeAfter(now);
			Date next2Data = cronExpression.getTimeAfter(nextData);
			if (nextData != null && next2Data != null) {
				long interval = TimeUnit.MILLISECONDS.toSeconds((next2Data.getTime() - nextData.getTime()));
				if (interval < SchedulerxConstants.SECOND_DELAY_MAX_VALUE) {
					timeType = TimeType.SECOND_DELAY.getValue();
					timeExpression = String.valueOf(interval < SchedulerxConstants.SECOND_DELAY_MIN_VALUE ?
							SchedulerxConstants.SECOND_DELAY_MIN_VALUE : interval);
				}
				else {
					timeType = TimeType.CRON.getValue();
					timeExpression = jobProperty.getCron();
				}
			}
			else {
				timeType = TimeType.CRON.getValue();
				timeExpression = jobProperty.getCron();
			}
		}
		else if (StringUtils.isNotEmpty(jobProperty.getOneTime())) {
			timeType = TimeType.ONE_TIME.getValue();
			timeExpression = jobProperty.getOneTime();
		}
		else {
			timeType = TimeType.API.getValue();
		}

		if (!jobConfigInfo.getDescription().equals(jobProperty.getDescription())
				|| !jobConfigInfo.getClassName().equals(jobProperty.getClassName())
				|| !jobConfigInfo.getParameters().equals(jobProperty.getJobParameter())
				|| !jobConfigInfo.getExecuteMode().equals(executeMode)
				|| jobConfigInfo.getTimeConfig().getTimeType() != timeType
				|| !jobConfigInfo.getTimeConfig().getTimeExpression().equals(timeExpression)) {

			UpdateJobRequest request = new UpdateJobRequest();
			request.setNamespace(properties.getNamespace());
			request.setNamespaceSource(namespaceSource);
			request.setGroupId(properties.getGroupId());
			request.setJobId(jobConfigInfo.getJobId());
			request.setName(jobConfigInfo.getName());
			request.setParameters(jobProperty.getJobParameter());

			//java任务
			if (jobProperty.getJobType().equals(JobType.JAVA.getKey())) {
				request.setClassName(jobProperty.getClassName());
			}
			request.setExecuteMode(executeMode);
			if (StringUtils.isNotEmpty(jobProperty.getDescription())) {
				request.setDescription(jobProperty.getDescription());
			}
			request.setTimeType(timeType);
			request.setTimeExpression(timeExpression);

			request.setTimeoutEnable(true);
			request.setTimeoutKillEnable(true);
			request.setSendChannel(SchedulerxConstants.JOB_ALARM_CHANNEL_DEFAULT);
			request.setFailEnable(true);
			request.setTimeout(SchedulerxConstants.JOB_TIMEOUT_DEFAULT);
			request.setMaxAttempt(SchedulerxConstants.JOB_RETRY_COUNT_DEFAULT);
			request.setAttemptInterval(SchedulerxConstants.JOB_RETRY_INTERVAL_DEFAULT);

			UpdateJobResponse response = client.getAcsResponse(request);
			if (response.getSuccess()) {
				logger.info("update schedulerx job successfully, jobId={}, jobName={}", jobConfigInfo.getJobId(), jobConfigInfo.getName());
			}
			else {
				throw new IOException("update schedulerx job failed, jobName=" + jobConfigInfo.getName() + ", message=" + response.getMessage());
			}
		}
	}

	/**
	 * Get namespace source.
	 *
	 * @return namespace source
	 */
	private String getNamespaceSource() {
		if (StringUtils.isEmpty(properties.getNamespaceSource())) {
			return SchedulerxConstants.NAMESPACE_SOURCE_SPRINGBOOT;
		}
		return properties.getNamespaceSource();
	}
}
