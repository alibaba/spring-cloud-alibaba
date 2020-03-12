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

import com.alibaba.alicloud.redis.actuate.endpoint.RedisDescribeAvailableResourceEndpoint;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.r_kvstore.model.v20150101.DescribeInstancesResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * The test cases for {@link RedisEndpointAutoConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		RedisEndpointAutoConfigurationTest.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
				// Please set "-Dspring.cloud.alicloud.access-key" for accessKey
				// Please set "-Dspring.cloud.alicloud.secret-key" for secretKey
				"management.endpoint.redis-describe-available-resource.enabled = true",
				"management.endpoints.web.exposure.include = *",
				"management.endpoints.jmx.exposure.include = *", })
@EnableAutoConfiguration
public class RedisEndpointAutoConfigurationTest {

	@Autowired
	private RedisDescribeAvailableResourceEndpoint redisDescribeAvailableResourceEndpoint;

	@Test
	public void testRedisDescribeAvailableResourceEndpoint() throws ClientException {

		DescribeInstancesResponse response = redisDescribeAvailableResourceEndpoint
				.execute("cn-hangzhou");

		assertNotNull(response);
	}
}
