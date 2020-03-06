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

package com.alibaba.alicloud.ans.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.ipms.NodeReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * @author xiaolongzuo
 */
public class AnsServiceRegistry implements ServiceRegistry<AnsRegistration> {

	private static final Logger log = LoggerFactory.getLogger(AnsServiceRegistry.class);

	private static final String SEPARATOR = ",";

	@Override
	public void register(AnsRegistration registration) {

		if (!registration.isRegisterEnabled()) {
			log.warn("Registration is disabled...");
			return;
		}
		if (StringUtils.isEmpty(registration.getServiceId())) {
			log.warn("No service to register for client...");
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
				log.info("INFO_ANS_REGISTER, {} {}:{} register finished", dom,
						registration.getAnsProperties().getClientIp(),
						registration.getAnsProperties().getClientPort());
			}
			catch (Exception e) {
				log.error("ERR_ANS_REGISTER, {} register failed...{},", dom,
						registration.toString(), e);
			}
		}
	}

	@Override
	public void deregister(AnsRegistration registration) {

		log.info("De-registering from ANSServer now...");

		if (StringUtils.isEmpty(registration.getServiceId())) {
			log.warn("No dom to de-register for client...");
			return;
		}

		try {
			NamingService.deRegDom(registration.getServiceId(), registration.getHost(),
					registration.getPort(), registration.getCluster());
		}
		catch (Exception e) {
			log.error("ERR_ANS_DEREGISTER, de-register failed...{},",
					registration.toString(), e);
		}

		log.info("De-registration finished.");
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
