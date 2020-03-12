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
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.r_kvstore.model.v20150101.DescribeInstancesRequest;
import com.aliyuncs.r_kvstore.model.v20150101.DescribeInstancesResponse;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import static com.aliyuncs.profile.DefaultProfile.getProfile;

/**
 * The {@link Endpoint} for Alibaba Cloud Redis's <a href=
 * "https://help.aliyun.com/document_detail/120580.html">DescribeAvailableResource</a>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
@Endpoint(id = "redisDescribeAvailableResource")
public class RedisDescribeAvailableResourceEndpoint extends AbstractRedisEndpoint {

	public RedisDescribeAvailableResourceEndpoint(AliCloudProperties aliCloudProperties) {
		super(aliCloudProperties);
	}

	@ReadOperation
	public DescribeInstancesResponse execute(@Selector String regionId)
			throws ClientException {
		DefaultProfile profile = getProfile(regionId, getAccessKey(), getSecretKey());
		IAcsClient client = new DefaultAcsClient(profile);
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setSysRegionId(regionId);
		return client.getAcsResponse(request);
	}

}
