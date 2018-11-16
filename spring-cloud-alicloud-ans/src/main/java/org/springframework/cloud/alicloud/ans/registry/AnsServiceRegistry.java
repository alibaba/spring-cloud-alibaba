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

package org.springframework.cloud.alicloud.ans.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.ipms.NodeReactor;

/**
 * @author xiaolongzuo
 */
public class AnsServiceRegistry implements ServiceRegistry<AnsRegistration> {

	private static Logger logger = LoggerFactory.getLogger(AnsServiceRegistry.class);

	private static final String SEPARATOR = ",";

	@Override
	public void register(AnsRegistration registration) {

		if (!registration.isRegisterEnabled()) {
			logger.info("Registration is disabled...");
			return;
		}
		if (StringUtils.isEmpty(registration.getServiceId())) {
			logger.info("No service to register for client...");
			return;
		}

		List<NodeReactor.Tag> tags = new ArrayList<>();
		for (Map.Entry<String, String> entry : registration.getAnsProperties().getTags()
				.entrySet()) {
			NodeReactor.Tag tag = new NodeReactor.Tag();
			tag.setName(entry.getKey());
			tag.setValue(entry.getValue());
			tags.add(tag);
		}

		for (String dom : registration.getServiceId().split(SEPARATOR)) {
			try {
				NamingService.regDom(dom, registration.getHost(), registration.getPort(),
						registration.getRegisterWeight(dom), registration.getCluster(),
						tags);
				logger.info("INFO_ANS_REGISTER, {} {}:{} register finished", dom,
						registration.getAnsProperties().getClientIp(),
						registration.getAnsProperties().getClientPort());
			}
			catch (Exception e) {
				logger.error("ERR_ANS_REGISTER, {} register failed...{},", dom,
						registration.toString(), e);
			}
		}
	}

	@Override
	public void deregister(AnsRegistration registration) {

		logger.info("De-registering from ANSServer now...");

		if (StringUtils.isEmpty(registration.getServiceId())) {
			logger.info("No dom to de-register for client...");
			return;
		}

		try {
			NamingService.deRegDom(registration.getServiceId(), registration.getHost(),
					registration.getPort(), registration.getCluster());
		}
		catch (Exception e) {
			logger.error("ERR_ANS_DEREGISTER, de-register failed...{},",
					registration.toString(), e);
		}

		logger.info("De-registration finished.");
	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(AnsRegistration registration, String status) {

	}

	@Override
	public <T> T getStatus(AnsRegistration registration) {
		return null;
	}

}
