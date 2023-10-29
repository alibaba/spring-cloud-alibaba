/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.metrics.FastCompass;
import com.alibaba.nacos.api.naming.pojo.Instance;

/**
 * @author xqw
 * @author 550588941@qq.com
 */

public class ServiceInstanceInfo {

	/**
	 * Current instance info.
	 */
	private Instance instance;

	/**
	 * Metrics data.
	 */
	private FastCompass compass;

	/**
	 * Instance remove time, It increases according to the number of removals.
	 */
	private Long removeTime;

	/**
	 * The percentage of services removed over a period of time. get data from metrics.
	 */
	private double removalRatio;

	/**
	 * Instance status.
	 */
	private boolean status;

	private AtomicInteger consecutiveErrors;

	public AtomicInteger getConsecutiveErrors() {
		return consecutiveErrors;
	}

	public void setConsecutiveErrors(AtomicInteger consecutiveErrors) {
		this.consecutiveErrors = consecutiveErrors;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Long getRemoveTime() {
		return removeTime;
	}

	public void setRemoveTime(Long removeTime) {
		this.removeTime = removeTime;
	}

	public double getRemovalRatio() {
		return removalRatio;
	}

	public void setRemovalRatio(double removalRatio) {
		this.removalRatio = removalRatio;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public FastCompass getCompass() {
		return compass;
	}

	public void setCompass(FastCompass compass) {
		this.compass = compass;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ServiceInstanceInfo{");
		sb.append("instance=").append(instance);
		sb.append(", compass=").append(compass);
		sb.append(", removeTime=").append(removeTime);
		sb.append(", removalRatio=").append(removalRatio);
		sb.append(", status=").append(status);
		sb.append(", consecutiveErrors=").append(consecutiveErrors);
		sb.append('}');
		return sb.toString();
	}
}
