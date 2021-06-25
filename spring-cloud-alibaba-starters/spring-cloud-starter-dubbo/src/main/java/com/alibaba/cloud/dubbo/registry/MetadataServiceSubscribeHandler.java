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

import java.util.List;

import com.alibaba.cloud.dubbo.util.DubboMetadataUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;

import org.springframework.cloud.client.ServiceInstance;

import static org.apache.dubbo.common.constants.CommonConstants.PROTOCOL_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MetadataServiceSubscribeHandler extends AbstractServiceSubscribeHandler {

	private final String appName;

	private final DubboMetadataUtils dubboMetadataUtils;

	public MetadataServiceSubscribeHandler(String appName, URL url,
			NotifyListener listener, DubboCloudRegistry registry,
			DubboMetadataUtils dubboMetadataUtils) {
		super(url, listener, registry);
		this.appName = appName;
		this.dubboMetadataUtils = dubboMetadataUtils;
	}

	@Override
	public void doInit() {
		logger.debug("Subscription app {} MetadataService handler init", appName);
		List<ServiceInstance> serviceInstances = registry.getServiceInstances(appName);
		subscribeDubboMetadataServiceURLs(url, listener, serviceInstances);
	}

	public void refresh(List<ServiceInstance> serviceInstances) {
		logger.debug("Subscription app {}, instance changed, new size = {}", appName,
				serviceInstances.size());
		subscribeDubboMetadataServiceURLs(url, listener, serviceInstances);
	}

	private void subscribeDubboMetadataServiceURLs(URL subscribedURL,
			NotifyListener listener, List<ServiceInstance> serviceInstances) {

		logger.debug("Subscription app {}, service instance changed to size {}", appName,
				serviceInstances.size());

		String serviceInterface = subscribedURL.getServiceInterface();
		String version = subscribedURL.getParameter(VERSION_KEY);
		String protocol = subscribedURL.getParameter(PROTOCOL_KEY);

		List<URL> urls = dubboMetadataUtils.getDubboMetadataServiceURLs(serviceInstances,
				serviceInterface, version, protocol);

		notifyAllSubscribedURLs(subscribedURL, urls, listener);
	}

}
