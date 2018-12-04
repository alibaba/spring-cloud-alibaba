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

package org.springframework.cloud.alibaba.nacos.refresh;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.alibaba.nacos.NacosPropertySourceRepository;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * On application start up, NacosContextRefresher add nacos listeners to all application
 * level dataIds, when there is a change in the data, listeners will refresh
 * configurations.
 *
 * @author juven.xuxb
 * @author pbting
 */
public class NacosContextRefresher implements ApplicationListener<ApplicationReadyEvent> {

	private Logger logger = LoggerFactory.getLogger(NacosContextRefresher.class);

	private final ContextRefresher contextRefresher;

	private final NacosConfigProperties properties;

	private final NacosRefreshProperties refreshProperties;

	private final NacosRefreshHistory refreshHistory;

	private final NacosPropertySourceRepository nacosPropertySourceRepository;

	private final ConfigService configService;

	private Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

	public NacosContextRefresher(ContextRefresher contextRefresher,
			NacosConfigProperties properties, NacosRefreshProperties refreshProperties,
			NacosRefreshHistory refreshHistory,
			NacosPropertySourceRepository nacosPropertySourceRepository,
			ConfigService configService) {
		this.contextRefresher = contextRefresher;
		this.properties = properties;
		this.refreshProperties = refreshProperties;
		this.refreshHistory = refreshHistory;
		this.nacosPropertySourceRepository = nacosPropertySourceRepository;
		this.configService = configService;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.registerNacosListenersForApplications();
	}

	private void registerNacosListenersForApplications() {
		if (refreshProperties.isEnabled()) {
			for (NacosPropertySource nacosPropertySource : nacosPropertySourceRepository
					.getAll()) {
				if (!nacosPropertySource.isRefreshable()) {
					continue;
				}

				String dataId = nacosPropertySource.getDataId();
				registerNacosListener(nacosPropertySource.getGroup(), dataId);
			}
		}
	}

	private void registerNacosListener(final String group, final String dataId) {

		Listener listener = listenerMap.computeIfAbsent(dataId, i -> new Listener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				String md5 = "";
				if (!StringUtils.isEmpty(configInfo)) {
					try {
						MessageDigest md = MessageDigest.getInstance("MD5");
						md5 = new BigInteger(1, md.digest(configInfo.getBytes("UTF-8")))
								.toString(16);
					}
					catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
						logger.warn("[Nacos] unable to get md5 for dataId: " + dataId, e);
					}
				}
				refreshHistory.add(dataId, md5);
				contextRefresher.refresh();
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});

		try {
			configService.addListener(dataId, group, listener);
		}
		catch (NacosException e) {
			e.printStackTrace();
		}
	}

}
