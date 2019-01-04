package org.springframework.cloud.alicloud.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author pbting
 */
public abstract class AbstractSmsService implements ISmsService {

	private ConcurrentHashMap<String, IAcsClient> acsClientConcurrentHashMap = new ConcurrentHashMap<>();

	public IAcsClient getHangZhouRegionClientProfile(String accessKeyId,
			String accessKeySecret) {

		return acsClientConcurrentHashMap.computeIfAbsent(
				getKey("cn-hangzhou", accessKeyId, accessKeySecret),
				(iacsClient) -> new DefaultAcsClient(DefaultProfile
						.getProfile("cn-hangzhou", accessKeyId, accessKeySecret)));
	}

	private String getKey(String regionId, String accessKeyId, String accessKeySecret) {

		return regionId + ":" + accessKeyId + ":" + accessKeySecret;
	}

}