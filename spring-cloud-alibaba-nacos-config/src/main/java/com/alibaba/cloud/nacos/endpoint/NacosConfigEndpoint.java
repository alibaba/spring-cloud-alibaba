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

package com.alibaba.cloud.nacos.endpoint;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alibaba.boot.nacos.common.PropertiesUtils;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME;

/**
 * Endpoint for Nacos, contains config data and refresh history.
 *
 * @author xiaojing
 */
@Endpoint(id = "nacos-config")
public class NacosConfigEndpoint implements ApplicationListener<NacosConfigMetadataEvent>,
		ApplicationContextAware {

	private final NacosConfigProperties properties;
	private final NacosRefreshHistory refreshHistory;
	private ApplicationContext applicationContext;
	private ThreadLocal<DateFormat> dateFormat = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

	private Map<String, JSONObject> nacosConfigMetadataMap = new HashMap<>();

	public NacosConfigEndpoint(NacosConfigProperties properties,
			NacosRefreshHistory refreshHistory) {
		this.properties = properties;
		this.refreshHistory = refreshHistory;
	}

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>(2);
		result.put("NacosConfigProperties", properties);

		Map<String, Object> cloud = new HashMap<>(16);
		List<NacosPropertySource> all = NacosPropertySourceRepository.getAll();

		List<Map<String, Object>> sources = new ArrayList<>();
		for (NacosPropertySource ps : all) {
			Map<String, Object> source = new HashMap<>(16);
			source.put("dataId", ps.getDataId());
			source.put("lastSynced", dateFormat.get().format(ps.getTimestamp()));
			sources.add(source);
		}
		cloud.put("Sources", sources);
		cloud.put("RefreshHistory", refreshHistory.getRecords());

		Map<String, Object> boot = new HashMap<>(16);

		if (!(ClassUtils.isAssignable(applicationContext.getEnvironment().getClass(),
				ConfigurableEnvironment.class))) {
			boot.put("error", "environment type not match ConfigurableEnvironment: "
					+ applicationContext.getEnvironment().getClass().getName());
		}
		else {

			boot.put("nacosConfigMetadata", nacosConfigMetadataMap.values());

			boot.put("nacosConfigGlobalProperties",
					PropertiesUtils.extractSafeProperties(applicationContext.getBean(
							CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME, Properties.class)));
		}

		result.put("nacosBoot", boot);
		result.put("nacosCloud", cloud);

		return result;
	}

	@Override
	public void onApplicationEvent(NacosConfigMetadataEvent event) {
		String key = buildMetadataKey(event);
		if (StringUtils.isNotEmpty(key) && !nacosConfigMetadataMap.containsKey(key)) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("groupId", event.getGroupId());
			jsonObject.put("dataId", event.getDataId());
			if (ClassUtils.isAssignable(event.getSource().getClass(),
					AnnotationMetadata.class)) {
				jsonObject.put("origin", "NacosPropertySource");
				jsonObject.put("target",
						((AnnotationMetadata) event.getSource()).getClassName());
			}
			else if (ClassUtils.isAssignable(event.getSource().getClass(),
					NacosConfigListener.class)) {
				jsonObject.put("origin", "NacosConfigListener");
				Method configListenerMethod = (Method) event.getAnnotatedElement();
				jsonObject.put("target",
						configListenerMethod.getDeclaringClass().getName() + ":"
								+ configListenerMethod.toString());
			}
			else if (ClassUtils.isAssignable(event.getSource().getClass(),
					NacosConfigurationProperties.class)) {
				jsonObject.put("origin", "NacosConfigurationProperties");
				jsonObject.put("target", event.getBeanType().getName());
			}
			else if (ClassUtils.isAssignable(event.getSource().getClass(),
					Element.class)) {
				jsonObject.put("origin", "NacosPropertySource");
				jsonObject.put("target", event.getXmlResource().toString());
			}
			else {
				throw new RuntimeException("unknown NacosConfigMetadataEvent");
			}
			nacosConfigMetadataMap.put(key, jsonObject);
		}
	}

	private String buildMetadataKey(NacosConfigMetadataEvent event) {
		if (event.getXmlResource() != null) {
			return event.getGroupId() + NacosUtils.SEPARATOR + event.getDataId()
					+ NacosUtils.SEPARATOR + event.getXmlResource();
		}
		else {
			if (event.getBeanType() == null && event.getAnnotatedElement() == null) {
				return StringUtils.EMPTY;
			}
			return event.getGroupId() + NacosUtils.SEPARATOR + event.getDataId()
					+ NacosUtils.SEPARATOR + event.getBeanType() + NacosUtils.SEPARATOR
					+ event.getAnnotatedElement();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
