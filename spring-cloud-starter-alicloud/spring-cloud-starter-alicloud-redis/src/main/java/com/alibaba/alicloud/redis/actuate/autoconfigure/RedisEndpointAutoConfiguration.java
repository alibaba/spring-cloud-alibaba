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
package com.alibaba.alicloud.redis.actuate.autoconfigure;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;
import com.alibaba.alicloud.context.AliCloudProperties;
import com.alibaba.alicloud.context.condition.ConditionalOnAliCloudEndpoint;
import com.alibaba.alicloud.redis.actuate.endpoint.RedisDescribeAvailableResourceEndpoint;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The endpoint Auto-Configuration for Alibaba Cloud Redis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
@ConditionalOnProperty(name = "spring.cloud.alicloud.redis.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAliCloudEndpoint
@PropertySource(name = "Alibaba Cloud Redis Endpoints Default Properties", value = "classpath:/META-INF/redis/default/actuator-endpoints.properties")
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AliCloudContextAutoConfiguration.class)
public class RedisEndpointAutoConfiguration {

	private final AliCloudProperties aliCloudProperties;

	public RedisEndpointAutoConfiguration(AliCloudProperties aliCloudProperties) {
		this.aliCloudProperties = aliCloudProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	public RedisDescribeAvailableResourceEndpoint redisDescribeAvailableResourceEndpoint() {
		return new RedisDescribeAvailableResourceEndpoint(aliCloudProperties);
	}
}
