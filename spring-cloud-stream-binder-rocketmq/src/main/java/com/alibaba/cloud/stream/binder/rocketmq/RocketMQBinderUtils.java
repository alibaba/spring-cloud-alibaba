/*
 * Copyright (C) 2018 the original author or authors.
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

package com.alibaba.cloud.stream.binder.rocketmq;

import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderUtils {

	public static RocketMQBinderConfigurationProperties mergeProperties(
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			RocketMQProperties rocketMQProperties) {
		RocketMQBinderConfigurationProperties result = new RocketMQBinderConfigurationProperties();
		if (StringUtils.isEmpty(rocketMQProperties.getNameServer())) {
			result.setNameServer(rocketBinderConfigurationProperties.getNameServer());
		}
		else {
			result.setNameServer(rocketMQProperties.getNameServer());
		}
		if (rocketMQProperties.getProducer() == null
				|| StringUtils.isEmpty(rocketMQProperties.getProducer().getAccessKey())) {
			result.setAccessKey(rocketBinderConfigurationProperties.getAccessKey());
		}
		else {
			result.setAccessKey(rocketMQProperties.getProducer().getAccessKey());
		}
		if (rocketMQProperties.getProducer() == null
				|| StringUtils.isEmpty(rocketMQProperties.getProducer().getSecretKey())) {
			result.setSecretKey(rocketBinderConfigurationProperties.getSecretKey());
		}
		else {
			result.setSecretKey(rocketMQProperties.getProducer().getSecretKey());
		}
		if (rocketMQProperties.getProducer() == null || StringUtils
				.isEmpty(rocketMQProperties.getProducer().getCustomizedTraceTopic())) {
			result.setCustomizedTraceTopic(
					rocketBinderConfigurationProperties.getCustomizedTraceTopic());
		}
		else {
			result.setCustomizedTraceTopic(
					rocketMQProperties.getProducer().getCustomizedTraceTopic());
		}
		if (rocketMQProperties.getProducer() != null
				&& rocketMQProperties.getProducer().isEnableMsgTrace()) {
			result.setEnableMsgTrace(Boolean.TRUE);
		}
		else {
			result.setEnableMsgTrace(
					rocketBinderConfigurationProperties.isEnableMsgTrace());
		}
		return result;
	}

}
