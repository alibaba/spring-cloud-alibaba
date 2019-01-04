package org.springframework.cloud.alicloud.sms.base;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;

/**
 * 用于接收云通信消息的临时token
 *
 */
public class TokenForAlicom {
	private String messageType;
	private String token;
	private Long expireTime;
	private String tempAccessKeyId;
	private String tempAccessKeySecret;
	private MNSClient client;
	private CloudQueue queue;

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public String getTempAccessKeyId() {
		return tempAccessKeyId;
	}

	public void setTempAccessKeyId(String tempAccessKeyId) {
		this.tempAccessKeyId = tempAccessKeyId;
	}

	public String getTempAccessKeySecret() {
		return tempAccessKeySecret;
	}

	public void setTempAccessKeySecret(String tempAccessKeySecret) {
		this.tempAccessKeySecret = tempAccessKeySecret;
	}

	public MNSClient getClient() {
		return client;
	}

	public void setClient(MNSClient client) {
		this.client = client;
	}

	public CloudQueue getQueue() {
		return queue;
	}

	public void setQueue(CloudQueue queue) {
		this.queue = queue;
	}

	public void closeClient() {
		if (client != null) {
			this.client.close();
		}
	}

}
