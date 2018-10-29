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

package org.springframework.cloud.alicloud.acm.refresh;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.listener.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alicloud.acm.AcmPropertySourceRepository;
import org.springframework.cloud.alicloud.acm.bootstrap.AcmPropertySource;
import org.springframework.cloud.alicloud.context.acm.AcmProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * On application start up, AcmContextRefresher add diamond listeners to all application
 * level dataIds, when there is a change in the data, listeners will refresh
 * configurations.
 *
 * @author juven.xuxb, 5/13/16.
 */
public class AcmContextRefresher implements ApplicationListener<ApplicationReadyEvent> {

	private Logger logger = LoggerFactory.getLogger(AcmContextRefresher.class);

	private final ContextRefresher contextRefresher;

	private final AcmProperties properties;

	private final AcmRefreshHistory refreshHistory;

	private final AcmPropertySourceRepository acmPropertySourceRepository;

	private Map<String, ConfigChangeListener> listenerMap = new ConcurrentHashMap<>(16);

	@Autowired
	private Environment environment;

	public AcmContextRefresher(ContextRefresher contextRefresher,
			AcmProperties properties, AcmRefreshHistory refreshHistory,
			AcmPropertySourceRepository acmPropertySourceRepository) {
		this.contextRefresher = contextRefresher;
		this.properties = properties;
		this.refreshHistory = refreshHistory;
		this.acmPropertySourceRepository = acmPropertySourceRepository;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.registerDiamondListenersForApplications();
	}

	private void registerDiamondListenersForApplications() {
		if (properties.isRefreshEnabled()) {
			for (AcmPropertySource acmPropertySource : acmPropertySourceRepository
					.getAll()) {
				if (acmPropertySource.isGroupLevel()) {
					continue;
				}
				String dataId = acmPropertySource.getDataId();
				registerDiamondListener(dataId);
			}
			if (acmPropertySourceRepository.getAll().isEmpty()) {

				String applicationName = environment
						.getProperty("spring.application.name");
				String dataId = applicationName + "." + properties.getFileExtension();

				registerDiamondListener(dataId);
			}
		}
	}

	private void registerDiamondListener(final String dataId) {

		ConfigChangeListener listener = listenerMap.computeIfAbsent(dataId,
				i -> new ConfigChangeListener() {
					@Override
					public void receiveConfigInfo(String configInfo) {
						String md5 = "";
						if (!StringUtils.isEmpty(configInfo)) {
							try {
								MessageDigest md = MessageDigest.getInstance("MD5");
								md5 = new BigInteger(1,
										md.digest(configInfo.getBytes("UTF-8")))
												.toString(16);
							}
							catch (NoSuchAlgorithmException
									| UnsupportedEncodingException e) {
								logger.warn("unable to get md5 for dataId: " + dataId, e);
							}
						}
						refreshHistory.add(dataId, md5);
						contextRefresher.refresh();
					}
				});
		ConfigService.addListener(dataId, properties.getGroup(), listener);
	}

}
