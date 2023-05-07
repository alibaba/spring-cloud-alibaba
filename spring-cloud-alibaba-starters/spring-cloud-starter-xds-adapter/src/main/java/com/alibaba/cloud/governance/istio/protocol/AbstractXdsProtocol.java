/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.istio.protocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.governance.istio.AggregateDiscoveryService;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.exception.XdsInitializationException;
import com.alibaba.cloud.governance.istio.filter.XdsResolveFilter;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public abstract class AbstractXdsProtocol<T>
		implements XdsProtocol<T>, XdsDecoder<T>, ApplicationContextAware {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractXdsProtocol.class);

	protected XdsConfigProperties xdsConfigProperties;

	protected List<XdsResolveFilter<List<T>>> filters = new ArrayList<>();

	private Set<String> resourceNames = new HashSet<>();

	/**
	 * send event to submodules.
	 */
	protected ApplicationContext applicationContext;

	private final AggregateDiscoveryService aggregateDiscoveryService;

	protected static CountDownLatch initCdl;

	public AbstractXdsProtocol(XdsConfigProperties xdsConfigProperties,
			AggregateDiscoveryService aggregateDiscoveryService) {
		this.xdsConfigProperties = xdsConfigProperties;
		this.aggregateDiscoveryService = aggregateDiscoveryService;
		initCdl = new CountDownLatch(1);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public synchronized void observeResource() {
		observeResource(null);
	}

	public synchronized void syncObserveResource() {
		try {
			observeResource();
			boolean flag = initCdl.await(30, TimeUnit.SECONDS);
			if (!flag) {
				throw new XdsInitializationException(
						"Timeout when init config from xds server");
			}
		}
		catch (Exception e) {
			throw new XdsInitializationException("Error on fetch xds config", e);
		}
	}

	@Override
	public synchronized void observeResource(Set<String> resourceNames) {
		String typeUrl = getTypeUrl();
		if (resourceNames == null) {
			resourceNames = new HashSet<>();
		}
		aggregateDiscoveryService.sendXdsRequest(typeUrl, resourceNames);
	}

	public Set<String> getResourceNames() {
		return resourceNames;
	}

	public abstract List<T> decodeXdsResponse(DiscoveryResponse response);

	protected Set<String> resolveResourceNames(List<T> resources) {
		return new HashSet<>();
	}

	protected void fireXdsFilters(List<T> resources) {
		try {
			this.resourceNames = resolveResourceNames(resources);
		}
		catch (Exception e) {
			log.error("Error on resolving resource names from {}", resources);
		}
		for (XdsResolveFilter<List<T>> filter : filters) {
			try {
				if (!filter.resolve(resources)) {
					return;
				}
			}
			catch (Exception e) {
				log.error("Error on executing Xds filter {}", filter.getClass().getName(),
						e);
			}
		}

	}

}
