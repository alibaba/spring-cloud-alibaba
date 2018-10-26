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

package org.springframework.cloud.alicloud.ans.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

/**
 * @author xiaolongzuo
 */
@Endpoint(id = "ans")
public class AnsEndpoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnsEndpoint.class);

	private AnsProperties ansProperties;

	public AnsEndpoint(AnsProperties ansProperties) {
		this.ansProperties = ansProperties;
	}

	/**
	 * @return ans endpoint
	 */
	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> ansEndpoint = new HashMap<>();
		LOGGER.info("ANS endpoint invoke, ansProperties is {}", ansProperties);
		ansEndpoint.put("ansProperties", ansProperties);

		Map<String, Object> subscribes = new HashMap<>();
		Set<String> subscribeServices = NamingService.getDomsSubscribed();
		for (String service : subscribeServices) {
			try {
				List<Host> hosts = NamingService.getHosts(service);
				subscribes.put(service, hosts);
			}
			catch (Exception ignoreException) {

			}
		}
		ansEndpoint.put("subscribes", subscribes);
		LOGGER.info("ANS endpoint invoke, subscribes is {}", subscribes);
		return ansEndpoint;
	}

}
