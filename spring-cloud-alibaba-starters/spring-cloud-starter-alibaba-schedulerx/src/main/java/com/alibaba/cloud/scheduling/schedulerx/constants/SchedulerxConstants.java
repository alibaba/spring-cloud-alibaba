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

package com.alibaba.cloud.scheduling.schedulerx.constants;

/**
 * Schedulerx constants.
 *
 * @author yaohui
 */
public final class SchedulerxConstants {

	/**
	 * Schedulerx default namespace source.
	 */
	public static final String NAMESPACE_SOURCE_SPRINGBOOT = "springboot";

	/**
	 * Aliyun pop product.
	 */
	public static final String ALIYUN_POP_PRODUCT = "schedulerx2";

	/**
	 * Aliyun pop endpoint.
	 */
	public static final String ALIYUN_POP_SCHEDULERX_ENDPOINT = "schedulerx.aliyuncs.com";

	/**
	 * Second delay max value.
	 */
	public static final int SECOND_DELAY_MAX_VALUE = 60;

	/**
	 * Second delay min value.
	 */
	public static final int SECOND_DELAY_MIN_VALUE = 1;

	/**
	 * Job timeout default value.
	 */
	public static final long JOB_TIMEOUT_DEFAULT = 3600L;

	/**
	 * Job retry count default value.
	 */
	public static final int JOB_RETRY_COUNT_DEFAULT = 3;

	/**
	 * Job retry interval default value.
	 */
	public static final int JOB_RETRY_INTERVAL_DEFAULT = 30;

	/**
	 * Job alarm channel default value.
	 */
	public static final String JOB_ALARM_CHANNEL_DEFAULT = "default";

	/**
	 * Job model mapreduce alias.
	 */
	public static final String JOB_MODEL_MAPREDUCE_ALIAS = "mapreduce";

	@Override
	public String toString() {
		return super.toString();
	}

	private SchedulerxConstants() {
		throw new AssertionError("Must not instantiate constant utility class");
	}

}
