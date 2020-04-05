/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.stream.binder.rocketmq.properties;

import java.util.Arrays;
import java.util.List;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import org.apache.rocketmq.common.MixAll;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.rocketmq.binder")
public class RocketMQBinderConfigurationProperties {

	/**
	 * The name server list for rocketMQ.
	 */
	private List<String> nameServer = Arrays
			.asList(RocketMQBinderConstants.DEFAULT_NAME_SERVER);

	/**
	 * The property of "access-key".
	 */
	private String accessKey;

	/**
	 * The property of "secret-key".
	 */
	private String secretKey;

	/**
	 * Switch flag instance for message trace.
	 */
	private boolean enableMsgTrace = true;

	/**
	 * The name value of message trace topic.If you don't config,you can use the default
	 * trace topic name.
	 */
	private String customizedTraceTopic = MixAll.RMQ_SYS_TRACE_TOPIC;

	public List<String> getNameServer() {
		return nameServer;
	}

	public void setNameServer(List<String> nameServer) {
		this.nameServer = nameServer;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public boolean isEnableMsgTrace() {
		return enableMsgTrace;
	}

	public void setEnableMsgTrace(boolean enableMsgTrace) {
		this.enableMsgTrace = enableMsgTrace;
	}

	public String getCustomizedTraceTopic() {
		return customizedTraceTopic;
	}

	public void setCustomizedTraceTopic(String customizedTraceTopic) {
		this.customizedTraceTopic = customizedTraceTopic;
	}

}
