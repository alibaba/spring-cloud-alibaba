/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.alicloud.sms;

import java.util.concurrent.ConcurrentHashMap;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;

/**
 *
 * @author pbting
 */
public abstract class AbstractSmsService implements ISmsService {

	private ConcurrentHashMap<String, IAcsClient> acsClientConcurrentHashMap = new ConcurrentHashMap<>();

	/**
	 *
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @return
	 */
	@Override
	public IAcsClient getHangZhouRegionClientProfile(String accessKeyId,
			String accessKeySecret) {

		String key = getKey("cn-hangzhou", accessKeyId, accessKeySecret);
		IAcsClient acsClient = acsClientConcurrentHashMap.get(key);
		if (acsClient == null) {
			synchronized (this) {
				acsClient = acsClientConcurrentHashMap.get(key);
				if (acsClient == null) {
					acsClient = new DefaultAcsClient(DefaultProfile
							.getProfile("cn-hangzhou", accessKeyId, accessKeySecret));
					acsClientConcurrentHashMap.put(key, acsClient);
				}
			}
		}

		return acsClient;
	}

	private String getKey(String regionId, String accessKeyId, String accessKeySecret) {

		return regionId + ":" + accessKeyId + ":" + accessKeySecret;
	}

}