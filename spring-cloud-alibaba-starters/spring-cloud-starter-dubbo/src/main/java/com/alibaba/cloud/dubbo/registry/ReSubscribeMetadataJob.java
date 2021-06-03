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

package com.alibaba.cloud.dubbo.registry;

import java.util.Map;
import java.util.Set;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For re subscribe URL from provider.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ReSubscribeMetadataJob implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger(ReSubscribeMetadataJob.class);

	private final String serviceName;

	private final DubboCloudRegistry dubboCloudRegistry;

	private final int errorCounts;

	public ReSubscribeMetadataJob(String serviceName,
			DubboCloudRegistry dubboCloudRegistry, int errorCounts) {
		this.errorCounts = errorCounts;
		this.serviceName = serviceName;
		this.dubboCloudRegistry = dubboCloudRegistry;
	}

	public ReSubscribeMetadataJob(String serviceName,
			DubboCloudRegistry dubboCloudRegistry) {
		this(serviceName, dubboCloudRegistry, 0);
	}

	@Override
	public void run() {
		if (dubboCloudRegistry.getReConnectJobMap().get(serviceName) != this) {
			return;
		}
		try {
			logger.info("reSubscribe, serviceName = {}, count = {}", serviceName,
					errorCounts);
			for (Map.Entry<URL, NotifyListener> entry : dubboCloudRegistry
					.getUrlNotifyListenerMap().entrySet()) {
				doRun(entry.getKey(), entry.getValue());
			}
			dubboCloudRegistry.getReConnectJobMap().remove(serviceName);
		}
		catch (Exception e) {
			logger.warn(String.format(
					"reSubscribe failed, serviceName = %s, try refresh again",
					serviceName), e);
			dubboCloudRegistry.addReSubscribeMetadataJob(serviceName, errorCounts + 1);
		}
	}

	private void doRun(URL url, NotifyListener listener) {
		Set<String> serviceNames = dubboCloudRegistry.getServices(url);

		if (serviceNames.contains(serviceName)) {
			dubboCloudRegistry.subscribeURLs(url, serviceNames, listener);
		}
	}

}
