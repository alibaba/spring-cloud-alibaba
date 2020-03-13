/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.alicloud.redis.env;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alibaba.alicloud.redis.env.RedisProperties.PROPERTY_PREFIX;

/**
 * The {@link ConfigurationProperties} for Alibaba Cloud Redis
 * 
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
@ConfigurationProperties(PROPERTY_PREFIX)
public class RedisProperties {

	/**
	 * The prefix of the property of {@link RedisProperties}
	 */
	public static final String PROPERTY_PREFIX = "spring.cloud.alicloud.redis";

	/**
	 * The property of {@link #getInstanceId()}, e,g. "r-bp1xxxxxxxxxxxxx"
	 */
	public static final String INSTANCE_ID_PROPERTY = PROPERTY_PREFIX + ".instance-id";

	/**
	 * Default region id of Redis
	 */
	private String defaultRegionId = "cn-hangzhou";

	/**
	 * The id of Redis instance
	 */
	private String instanceId;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getDefaultRegionId() {
		return defaultRegionId;
	}

	public void setDefaultRegionId(String defaultRegionId) {
		this.defaultRegionId = defaultRegionId;
	}
}
