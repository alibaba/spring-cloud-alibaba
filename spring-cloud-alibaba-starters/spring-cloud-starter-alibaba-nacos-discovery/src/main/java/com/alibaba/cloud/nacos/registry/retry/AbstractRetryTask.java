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

package com.alibaba.cloud.nacos.registry.retry;

import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.nacos.common.timer.Timeout;
import com.alibaba.cloud.nacos.common.timer.Timer;
import com.alibaba.cloud.nacos.common.timer.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * AbstractRetryTask.
 */
public abstract class AbstractRetryTask implements TimerTask {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRetryTask.class);

	/**
	 * registration for retry task.
	 */
	protected Registration registration;

	/**
	 * registry for this task.
	 */
	protected ServiceRegistry registry;

	/**
	 * task name for this task.
	 */
	private final String taskName;

	/**
	 * times of retry. retry task is execute in single thread so that the times is not
	 * need volatile.
	 */
	private int times = 1;

	private volatile boolean cancel = false;

	AbstractRetryTask(Registration registration, ServiceRegistry registry,
			String taskName) {
		this.registration = registration;
		this.registry = registry;
		this.taskName = taskName;
	}

	public void cancel() {
		cancel = true;
	}

	public boolean isCancel() {
		return cancel;
	}

	protected void reput(Timeout timeout, long tick) {
		if (timeout == null) {
			throw new IllegalArgumentException();
		}

		Timer timer = timeout.timer();
		if (timer.isStop() || timeout.isCancelled() || isCancel()) {
			return;
		}
		times++;
		timer.newTimeout(timeout.task(), tick, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run(Timeout timeout) throws Exception {

		long retryPeriod = getRetryPeriod();
		int retryTimes = getRetryTimes();

		if (timeout.isCancelled() || timeout.timer().isStop() || isCancel()) {
			// other thread cancel this timeout or stop the timer.
			return;
		}
		if (times > retryTimes) {
			// reach the most times of retry.
			logger.warn(
					"Final failed to execute task {}, uri: {}, servicdId: {}, retry {} times. ",
					taskName, registration.getUri(), registration.getServiceId(),
					retryTimes);
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info(taskName + " : " + registration.getUri() + " : "
					+ registration.getServiceId());
		}
		try {
			doRetry(registration, registry, timeout);
		}
		catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
			logger.warn("Failed to execute task " + taskName + ", url: "
					+ registration.getUri() + ", serviceId: "
					+ registration.getServiceId() + ", waiting for again, cause:"
					+ t.getMessage(), t);
			// reput this task when catch exception.
			reput(timeout, retryPeriod);
		}
	}

	protected abstract void doRetry(Registration registration, ServiceRegistry registry,
			Timeout timeout) throws Exception;

	protected abstract int getRetryTimes();

	protected abstract long getRetryPeriod();

}
