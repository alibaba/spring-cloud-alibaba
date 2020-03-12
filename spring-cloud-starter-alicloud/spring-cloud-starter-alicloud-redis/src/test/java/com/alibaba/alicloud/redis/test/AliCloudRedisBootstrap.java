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
package com.alibaba.alicloud.redis.test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * A Spring Boot bootstrap for Alibaba Cloud Redis
 * 
 * @since 2.2.1
 */
@EnableAutoConfiguration
public class AliCloudRedisBootstrap {

	public static void main(String[] args) {
		new SpringApplicationBuilder(AliCloudRedisBootstrap.class)
				.properties(
						"management.endpoint.redisDescribeAvailableResource.enabled=true",
						"spring.jmx.enabled=true",
						"management.endpoints.web.exposure.include=*",
						"management.endpoints.jmx.exposure.include=*")
				.run(args);
	}
}
