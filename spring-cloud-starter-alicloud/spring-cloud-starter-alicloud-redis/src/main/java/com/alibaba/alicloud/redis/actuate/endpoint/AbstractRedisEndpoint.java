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
package com.alibaba.alicloud.redis.actuate.endpoint;

import com.alibaba.alicloud.context.AliCloudProperties;

/**
 * The abstract implementation for Alibaba Cloud Redis
 * 
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
public abstract class AbstractRedisEndpoint {

	private final AliCloudProperties aliCloudProperties;

	public AbstractRedisEndpoint(AliCloudProperties aliCloudProperties) {
		this.aliCloudProperties = aliCloudProperties;
	}

	public AliCloudProperties getAliCloudProperties() {
		return aliCloudProperties;
	}

	public String getAccessKey() {
		return getAliCloudProperties().getAccessKey();
	}

	public String getSecretKey() {
		return getAliCloudProperties().getSecretKey();
	}
}
