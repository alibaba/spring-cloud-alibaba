/*
 * Copyright 2013-2023 the original author or authors.
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

import com.alibaba.cloud.scheduling.schedulerx.JobProperty;
import com.alibaba.cloud.scheduling.schedulerx.SchedulerxProperties;
import com.alibaba.cloud.scheduling.schedulerx.util.CronExpression;
import com.alibaba.schedulerx.common.domain.ContactInfo;
import com.alibaba.schedulerx.common.domain.JobType;
import com.alibaba.schedulerx.common.domain.TimeType;
import com.alibaba.schedulerx.common.sdk.common.MonitorConfig;
import com.alibaba.schedulerx.common.util.JsonUtil;
import com.alibaba.schedulerx.common.util.StringUtils;
import com.alibaba.schedulerx.worker.log.LogFactory;
import com.alibaba.schedulerx.worker.log.Logger;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.InstanceProfileCredentialsProvider;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JobSyncService
 * @author xiaomeng.hxm
 */
public class JobSyncService {
    
    private static final Logger LOGGER = LogFactory.getLogger(JobSyncService.class);
    
    private static final String NAMESPACE_SOURCE_SPRINGBOOT = "springboot";
    
    @Autowired
    private SchedulerxProperties properties;

    private DefaultAcsClient client;

    private synchronized DefaultAcsClient getClient() {
        //build aliyun pop client
        if (client == null) {
            DefaultProfile.addEndpoint(properties.getRegionId(), "schedulerx2", "schedulerx.aliyuncs.com");
            if (StringUtils.isNotEmpty(properties.getAliyunRamRole())) {
                DefaultProfile profile = DefaultProfile.getProfile(properties.getRegionId());
                InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(
                        properties.getAliyunRamRole());
                client = new DefaultAcsClient(profile, provider);
            } else {
                DefaultProfile defaultProfile = DefaultProfile.getProfile(properties.getRegionId(),
                        properties.getAliyunAccessKey(),
                        properties.getAliyunSecretKey());
                client = new DefaultAcsClient(defaultProfile);
            }
        }
        return client;
    }

    /**
     * Sync job config
     * @param jobs
     * @throws Exception
     */
    public synchronized void syncJobs(Map<String, JobProperty> jobs, String namespaceSource) throws Exception {
        DefaultAcsClient client = getClient();
        for (Entry<String, JobProperty> entry : jobs.entrySet()) {
            String jobName = entry.getKey();
            JobProperty jobProperty = entry.getValue();
            JobConfigInfo jobConfigInfo = getJob(client, jobName, namespaceSource);
            if (jobConfigInfo == null) {
                createJob(client, jobName, jobProperty, namespaceSource);
            } else if (jobProperty.isOverwrite()) {
                updateJob(client, jobConfigInfo, jobProperty, namespaceSource);
            }
        }
    }
    
    public void syncJobs() throws Exception {
        // 1. create namespace
        if (syncNamespace(getClient())){
            // 2. create app group
            if(syncAppGroup(getClient())){
                syncJobs(properties.getJobs(), getNamespaceSource());
                properties.setNamespaceSource(getNamespaceSource());
            }
        }
    }
    
    public boolean syncNamespace(DefaultAcsClient client) throws Exception {
        if (StringUtils.isEmpty(properties.getNamespace())) {
            LOGGER.error("please set {}.namespace", SchedulerxProperties.CONFIG_PREFIX);
            throw new IOException(String.format("please set %s.namespace", SchedulerxProperties.CONFIG_PREFIX));
        }
        
        if (StringUtils.isEmpty(properties.getNamespaceName())) {
            LOGGER.error("please set {}.namespaceName", SchedulerxProperties.CONFIG_PREFIX);
            throw new IOException(String.format("please set %s.namespaceName", SchedulerxProperties.CONFIG_PREFIX));
        }
        
        CreateNamespaceRequest request = new CreateNamespaceRequest();
        request.setUid(properties.getNamespace());
        request.setName(properties.getNamespaceName());
        request.setSource(getNamespaceSource());
        CreateNamespaceResponse response = client.getAcsResponse(request);
        if (response.getSuccess()) {
            LOGGER.info(JsonUtil.toJson(response));
            return true;
        } else {
            throw new IOException(response.getMessage());
        }
        
    }
    
    /**
     * 
     * @param client
     * @return true if create new appGroup
     * @throws Exception
     */
    public boolean syncAppGroup(DefaultAcsClient client) throws Exception {
        if (StringUtils.isEmpty(properties.getAppName())) {
            LOGGER.error("please set {}.appName", SchedulerxProperties.CONFIG_PREFIX);
            throw new IOException(String.format("please set %s.appName", SchedulerxProperties.CONFIG_PREFIX));
        }
        
        if (StringUtils.isEmpty(properties.getAppKey())) {
            LOGGER.error("please set {}.appKey", SchedulerxProperties.CONFIG_PREFIX);
            throw new IOException(String.format("please set %s.appKey", SchedulerxProperties.CONFIG_PREFIX));
        }
        
        if (StringUtils.isEmpty(properties.getGroupId())) {
            LOGGER.error("please set {}.groupId", SchedulerxProperties.CONFIG_PREFIX);
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
            LOGGER.info(JsonUtil.toJson(response));
            return true;
        } else {
            throw new IOException(response.getMessage());
        }
    }
    
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
    
    private void createJob(DefaultAcsClient client, String jobName, JobProperty jobProperty, String namespaceSource) throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setNamespace(properties.getNamespace());
        request.setNamespaceSource(namespaceSource);
        request.setGroupId(properties.getGroupId());
        request.setName(jobName);
        request.setParameters(jobProperty.getJobParameter());

        if (jobProperty.getJobType().equals(JobType.JAVA.getKey())) {
            request.setJobType("java");
            request.setClassName(jobProperty.getClassName());
        } else {
            request.setJobType(jobProperty.getJobType());
        }
        
        if (jobProperty.getJobModel().equals("mapreduce")) {
            request.setExecuteMode("batch");
        } else {
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
            if(nextData != null && next2Data != null) {
                long interval = (next2Data.getTime() - nextData.getTime()) / 1000;
                if (interval < 60) {
                    request.setTimeType(TimeType.SECOND_DELAY.getValue());
                    request.setTimeExpression(String.valueOf(interval<1?1:interval));
                }else {
                    request.setTimeType(TimeType.CRON.getValue());
                    request.setTimeExpression(jobProperty.getCron());
                }
            }else {
                request.setTimeType(TimeType.CRON.getValue());
                request.setTimeExpression(jobProperty.getCron());
            }
        } else if (StringUtils.isNotEmpty(jobProperty.getOneTime())) {
            request.setTimeType(TimeType.ONE_TIME.getValue());
            request.setTimeExpression(jobProperty.getOneTime());
        } else {
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
        request.setSendChannel("default");
        request.setFailEnable(true);
        request.setTimeout(3600L);
        request.setMaxAttempt(3);
        request.setAttemptInterval(30);
        CreateJobResponse response = client.getAcsResponse(request);
        if (response.getSuccess()) {
            LOGGER.info("create schedulerx job successfully, jobId={}, jobName={}", response.getData().getJobId(), jobName);
        } else {
            throw new IOException("create schedulerx job failed, jobName=" + jobName + ", message=" + response.getMessage());
        }
    }
    
    private void updateJob(DefaultAcsClient client, JobConfigInfo jobConfigInfo, JobProperty jobProperty, String namespaceSource) throws Exception {
        String executeMode = jobProperty.getJobModel();
        if (jobProperty.getJobModel().equals("mapreduce")) {
            executeMode = "batch";
        }
        int timeType = TimeType.CRON.getValue();
        String timeExpression = null;
        if (StringUtils.isNotEmpty(jobProperty.getCron()) && StringUtils.isNotEmpty(jobProperty.getOneTime())) {
            throw new IOException("cron and oneTime shouldn't set together");
        }
        if (StringUtils.isNotEmpty(jobProperty.getCron())) {            
            CronExpression cronExpression = new CronExpression(jobProperty.getCron());
            Date now = new Date();
            Date nextData = cronExpression.getTimeAfter(now);
            Date next2Data = cronExpression.getTimeAfter(nextData);
            if(nextData != null && next2Data != null) {
                long interval = (next2Data.getTime() - nextData.getTime()) / 1000;
                if (interval < 60) {
                    timeType = TimeType.SECOND_DELAY.getValue();
                    timeExpression = String.valueOf(interval<1?1:interval);
                }else {
                    timeType = TimeType.CRON.getValue();
                    timeExpression = jobProperty.getCron();
                }
            }else {
                timeType = TimeType.CRON.getValue();
                timeExpression = jobProperty.getCron();
            }
        } else if (StringUtils.isNotEmpty(jobProperty.getOneTime())) {
            timeType = TimeType.ONE_TIME.getValue();
            timeExpression = jobProperty.getOneTime();
        } else {
            timeType = TimeType.API.getValue();
        }
        
        boolean needUpdate = false;
        if (!jobConfigInfo.getDescription().equals(jobProperty.getDescription())
            || !jobConfigInfo.getClassName().equals(jobProperty.getClassName())
            || !jobConfigInfo.getParameters().equals(jobProperty.getJobParameter())
            || !jobConfigInfo.getExecuteMode().equals(executeMode)
            || jobConfigInfo.getTimeConfig().getTimeType() != timeType
            || !jobConfigInfo.getTimeConfig().getTimeExpression().equals(timeExpression)) {
            needUpdate = true;
            
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
            request.setSendChannel("default");
            request.setFailEnable(true);
            request.setTimeout(3600L);
            request.setMaxAttempt(3);
            request.setAttemptInterval(30);
            
            UpdateJobResponse response = client.getAcsResponse(request);
            if (response.getSuccess()) {
                LOGGER.info("update schedulerx job successfully, jobId={}, jobName={}", jobConfigInfo.getJobId(), jobConfigInfo.getName());
            } else {
                throw new IOException("update schedulerx job failed, jobName=" + jobConfigInfo.getName() + ", message=" + response.getMessage());
            }
        }
    }

    /**
     * Get namespace source
     * @return
     */
    private String getNamespaceSource() {
        if (StringUtils.isEmpty(properties.getNamespaceSource())) {
            return NAMESPACE_SOURCE_SPRINGBOOT;
        }
        return properties.getNamespaceSource();
    }
}
