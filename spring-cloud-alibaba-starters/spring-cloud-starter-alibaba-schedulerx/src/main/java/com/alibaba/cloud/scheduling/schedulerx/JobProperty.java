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

import com.alibaba.schedulerx.common.domain.ExecuteMode;
import com.alibaba.schedulerx.common.domain.JobType;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Job property.
 *
 * @author xiaomeng.hxm
 */
@ConfigurationProperties(prefix = SchedulerxProperties.CONFIG_PREFIX)
public final class JobProperty {

	private @Nullable String jobName;

	private String jobType = JobType.JAVA.getKey();

	private String jobModel = ExecuteMode.STANDALONE.getKey();

	private @Nullable String className;

	private @Nullable String content;

	private @Nullable Integer timeType;

	private @Nullable String timeExpression;

	private @Nullable String cron;

	private @Nullable String oneTime;

	private @Nullable String jobParameter;

	private @Nullable String description;

	private boolean overwrite = false;

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getJobModel() {
		return jobModel;
	}

	public void setJobModel(String jobModel) {
		this.jobModel = jobModel;
	}

	public @Nullable String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public @Nullable String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public @Nullable String getOneTime() {
		return oneTime;
	}

	public void setOneTime(String oneTime) {
		this.oneTime = oneTime;
	}

	public @Nullable String getJobParameter() {
		return jobParameter;
	}

	public void setJobParameter(String jobParameter) {
		this.jobParameter = jobParameter;
	}

	public @Nullable String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public @Nullable String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public @Nullable String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public @Nullable Integer getTimeType() {
		return timeType;
	}

	public void setTimeType(Integer timeType) {
		this.timeType = timeType;
	}

	public @Nullable String getTimeExpression() {
		return timeExpression;
	}

	public void setTimeExpression(String timeExpression) {
		this.timeExpression = timeExpression;
	}
}
