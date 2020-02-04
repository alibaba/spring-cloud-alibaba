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

package com.alibaba.cloud.nacos.client;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 * @author pbting
 */
public class NacosPropertySourceBuilder {

	private static final Logger log = LoggerFactory
			.getLogger(NacosPropertySourceBuilder.class);

	private static final Map<String, Object> EMPTY_MAP = new LinkedHashMap();

	private ConfigService configService;

	private long timeout;

	public NacosPropertySourceBuilder(ConfigService configService, long timeout) {
		this.configService = configService;
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * @param dataId Nacos dataId
	 * @param group Nacos group
	 */
	NacosPropertySource build(String dataId, String group, String fileExtension,
			boolean isRefreshable) {
		Map<String, Object> p = loadNacosData(dataId, group, fileExtension);
		NacosPropertySource nacosPropertySource = new NacosPropertySource(group, dataId,
				p, new Date(), isRefreshable);
		NacosPropertySourceRepository.collectNacosPropertySource(nacosPropertySource);
		return nacosPropertySource;
	}

	private Map<String, Object> loadNacosData(String dataId, String group,
			String fileExtension) {
		String data = null;
		try {
			data = configService.getConfig(dataId, group, timeout);
			if (StringUtils.isEmpty(data)) {
				log.warn(
						"Ignore the empty nacos configuration and get it based on dataId[{}] & group[{}]",
						dataId, group);
				return EMPTY_MAP;
			}
			if (log.isDebugEnabled()) {
				log.debug(String.format(
						"Loading nacos data, dataId: '%s', group: '%s', data: %s", dataId,
						group, data));
			}

			// TODO 文件格式解析能力统一下沉到 nacos-spring-context 中

			Map<String, Object> dataMap = NacosDataParserHandler.getInstance()
					.parseNacosData(data, fileExtension);
			return dataMap == null ? EMPTY_MAP : dataMap;
		}
		catch (NacosException e) {
			log.error("get data from Nacos error,dataId:{}, ", dataId, e);
		}
		catch (Exception e) {
			log.error("parse data from Nacos error,dataId:{},data:{},", dataId, data, e);
		}
		return EMPTY_MAP;
	}

}
