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

package com.alibaba.cloud.stream.binder.rocketmq.utils;

import com.alibaba.cloud.stream.binder.rocketmq.contants.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQCommonProperties;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.RPCHook;

import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public final class RocketMQUtils {

	private RocketMQUtils() {
	}

	public static <T extends RocketMQCommonProperties> T mergeRocketMQProperties(
			RocketMQBinderConfigurationProperties binderConfigurationProperties,
			T mqProperties) {
		if (null == binderConfigurationProperties || mqProperties == null) {
			return mqProperties;
		}
		if (StringUtils.isEmpty(mqProperties.getNameServer())) {
			mqProperties.setNameServer(binderConfigurationProperties.getNameServer());
		}
		if (StringUtils.isEmpty(mqProperties.getSecretKey())) {
			mqProperties.setSecretKey(binderConfigurationProperties.getSecretKey());
		}
		if (StringUtils.isEmpty(mqProperties.getAccessKey())) {
			mqProperties.setAccessKey(binderConfigurationProperties.getAccessKey());
		}
		if (StringUtils.isEmpty(mqProperties.getAccessChannel())) {
			mqProperties
					.setAccessChannel(binderConfigurationProperties.getAccessChannel());
		}
		if (StringUtils.isEmpty(mqProperties.getNamespace())) {
			mqProperties.setNamespace(binderConfigurationProperties.getNamespace());
		}
		if (StringUtils.isEmpty(mqProperties.getGroup())) {
			mqProperties.setGroup(binderConfigurationProperties.getGroup());
		}
		if (StringUtils.isEmpty(mqProperties.getCustomizedTraceTopic())) {
			mqProperties.setCustomizedTraceTopic(
					binderConfigurationProperties.getCustomizedTraceTopic());
		}
		if (StringUtils.isEmpty(mqProperties.getUnitName())) {
			mqProperties.setUnitName(binderConfigurationProperties.getUnitName());
		}
		mqProperties.setNameServer(getNameServerStr(mqProperties.getNameServer()));
		return mqProperties;
	}

	public static String getInstanceName(RPCHook rpcHook, String identify) {
		String separator = "|";
		StringBuilder instanceName = new StringBuilder();
		if (null != rpcHook) {
			SessionCredentials sessionCredentials = ((AclClientRPCHook) rpcHook)
					.getSessionCredentials();
			instanceName.append(sessionCredentials.getAccessKey()).append(separator)
					.append(sessionCredentials.getSecretKey()).append(separator);
		}
		instanceName.append(identify).append(separator).append(UtilAll.getPid());
		return instanceName.toString();
	}

	public static String getNameServerStr(String nameServer) {
		if (StringUtils.isEmpty(nameServer)) {
			return RocketMQConst.DEFAULT_NAME_SERVER;
		}
		return nameServer.replaceAll(",", ";");
	}

	private static final String SQL = "sql:";

	public static MessageSelector getMessageSelector(String expression) {
		if (StringUtils.hasText(expression) && expression.startsWith(SQL)) {
			return MessageSelector.bySql(expression.replaceFirst(SQL, ""));
		}
		return MessageSelector.byTag(expression);
	}

}
