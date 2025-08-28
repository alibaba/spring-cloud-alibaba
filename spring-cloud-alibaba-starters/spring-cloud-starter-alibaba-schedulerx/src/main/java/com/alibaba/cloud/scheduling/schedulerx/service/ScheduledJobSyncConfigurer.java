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

package com.alibaba.cloud.scheduling.schedulerx.service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.scheduling.schedulerx.JobProperty;
import com.alibaba.cloud.scheduling.schedulerx.SchedulerxProperties;
import com.alibaba.schedulerx.common.domain.ExecuteMode;
import com.alibaba.schedulerx.common.domain.JobType;
import com.alibaba.schedulerx.common.domain.Pair;
import com.alibaba.schedulerx.common.domain.TimeType;
import com.alibaba.schedulerx.common.util.JsonUtil;
import com.alibaba.schedulerx.common.util.StringUtils;
import com.alibaba.schedulerx.scheduling.annotation.SchedulerX;
import com.alibaba.schedulerx.worker.domain.SpringScheduleProfile;
import com.alibaba.schedulerx.worker.log.LogFactory;
import com.alibaba.schedulerx.worker.log.Logger;
import com.alibaba.schedulerx.worker.processor.springscheduling.SchedulerxJobRegister;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Spring scheduled job sync.
 *
 * @author yaohui
 * @create 2022/8/17 下午2:21
 **/
public class ScheduledJobSyncConfigurer implements SchedulingConfigurer {

	private static final Logger logger = LogFactory.getLogger(ScheduledJobSyncConfigurer.class);

	@Autowired
	private JobSyncService jobSyncService;

	@Autowired
	private SchedulerxProperties properties;

	@Value("${" + SchedulerxProperties.CONFIG_PREFIX + ".task-overwrite:false}")
	private Boolean overwrite = false;

	@Value("${" + SchedulerxProperties.CONFIG_PREFIX + ".task-model-default:broadcast}")
	private String defaultModel = ExecuteMode.BROADCAST.getKey();

	private boolean isValidModel(String mode) {
		if (mode == null) {
			return false;
		}
		return (ExecuteMode.BROADCAST.getKey().equals(mode) || ExecuteMode.STANDALONE.getKey().equals(mode));
	}

	private JobProperty convertToJobProperty(Task task, Object target, Method method) {
		JobProperty jobProperty = new JobProperty();
		Class targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (ClassUtils.isCglibProxyClass(targetClass)) {
			targetClass = ClassUtils.getUserClass(target);
		}
		String jobName = targetClass.getSimpleName() + "_" + method.getName();
		String model = this.defaultModel;

		if (task != null && task instanceof CronTask) {
			String expression = ((CronTask) task).getExpression();
			jobProperty.setCron(expression);
		}

		if (task != null && task instanceof IntervalTask) {
			long interval = ((IntervalTask) task).getInterval() / 1000;
			interval = interval < 1 ? 1 : interval;
			if (interval < 60) {
				jobProperty.setTimeType(TimeType.SECOND_DELAY.getValue());
			}
			else {
				jobProperty.setTimeType(TimeType.FIXED_RATE.getValue());
			}
			jobProperty.setTimeExpression(String.valueOf(interval));
		}

		SchedulerX schedulerXMethod = AnnotatedElementUtils.getMergedAnnotation(method, SchedulerX.class);
		if (schedulerXMethod != null) {
			if (StringUtils.isNotEmpty(schedulerXMethod.name())) {
				jobName = schedulerXMethod.name();
			}
			if (isValidModel(schedulerXMethod.model())) {
				model = schedulerXMethod.model();
			}
			if (StringUtils.isNotEmpty(schedulerXMethod.cron())) {
				jobProperty.setCron(schedulerXMethod.cron());
			}
			if (schedulerXMethod.fixedRate() > 0) {
				long interval = schedulerXMethod.timeUnit().toSeconds(schedulerXMethod.fixedRate());
				interval = interval < 1 ? 1 : interval;
				if (interval < 60) {
					jobProperty.setTimeType(TimeType.SECOND_DELAY.getValue());
				}
				else {
					jobProperty.setTimeType(TimeType.FIXED_RATE.getValue());
				}
				jobProperty.setTimeExpression(String.valueOf(interval));
			}
		}

		jobProperty.setJobName(jobName);
		jobProperty.setJobType(JobType.SPRINGSCHEDULE.getKey());
		jobProperty.setJobModel(model);
		SpringScheduleProfile profile = new SpringScheduleProfile();
		profile.setClassName(targetClass.getName());
		profile.setMethod(method.getName());
		jobProperty.setContent(JsonUtil.toJson(profile));
		jobProperty.setOverwrite(overwrite);
		return jobProperty;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		logger.info("spring scheduled job is not empty, start to sync jobs...");
		try {
			Map<String, JobProperty> jobs = new HashMap<>();
			if (!CollectionUtils.isEmpty(taskRegistrar.getCronTaskList())) {
				for (CronTask cronTask : taskRegistrar.getCronTaskList()) {
					if (cronTask.getRunnable() instanceof ScheduledMethodRunnable) {
						ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) cronTask.getRunnable();
						JobProperty jobProperty = convertToJobProperty(cronTask, runnable.getTarget(), runnable.getMethod());
						jobs.put(jobProperty.getJobName(), jobProperty);
					}
				}
			}
			if (!CollectionUtils.isEmpty(taskRegistrar.getFixedDelayTaskList())) {
				for (IntervalTask intervalTask : taskRegistrar.getFixedDelayTaskList()) {
					if (intervalTask.getRunnable() instanceof ScheduledMethodRunnable) {
						ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) intervalTask.getRunnable();
						JobProperty jobProperty = convertToJobProperty(intervalTask, runnable.getTarget(), runnable.getMethod());
						jobs.put(jobProperty.getJobName(), jobProperty);
					}
				}
			}
			if (!CollectionUtils.isEmpty(taskRegistrar.getFixedRateTaskList())) {
				for (IntervalTask intervalTask : taskRegistrar.getFixedRateTaskList()) {
					if (intervalTask.getRunnable() instanceof ScheduledMethodRunnable) {
						ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) intervalTask.getRunnable();
						JobProperty jobProperty = convertToJobProperty(intervalTask, runnable.getTarget(), runnable.getMethod());
						jobs.put(jobProperty.getJobName(), jobProperty);
					}
				}
			}

			// 获取仅SchedulerX注解任务
			Collection<Pair<Object, Method>> schedulerXTasks = SchedulerxJobRegister.getInstance().getSchedulerXTaskTargets();
			if (schedulerXTasks != null && schedulerXTasks.size() > 0) {
				for (Pair<Object, Method> task : schedulerXTasks) {
					JobProperty jobProperty = convertToJobProperty(null, task.getFirst(), task.getSecond());
					jobs.put(jobProperty.getJobName(), jobProperty);
				}
			}

			jobSyncService.syncJobs(jobs, properties.getNamespaceSource());
			logger.info("spring scheduled job is not empty, sync jobs finished.");
		}
		catch (Exception e) {
			logger.info("spring scheduled job is not empty, sync jobs failed.", e);
			throw new RuntimeException(e);
		}
	}
}
