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

package com.alibaba.cloud.dubbo.service;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import com.alibaba.cloud.dubbo.metadata.DubboRestServiceMetadata;
import com.alibaba.cloud.dubbo.metadata.ServiceRestMetadata;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;

import static java.util.Collections.emptyMap;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;

/**
 * Dubbo {@link GenericService} Factory.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboGenericServiceFactory {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<String, ReferenceBean<GenericService>> cache = new ConcurrentHashMap<>();

	@Autowired
	private ObjectProvider<List<RegistryConfig>> registryConfigs;

	public GenericService create(DubboRestServiceMetadata dubboServiceMetadata,
			Map<String, Object> dubboTranslatedAttributes) {

		ReferenceBean<GenericService> referenceBean = build(
				dubboServiceMetadata.getServiceRestMetadata(), dubboTranslatedAttributes);

		return referenceBean == null ? null : referenceBean.get();
	}

	public GenericService create(String serviceName, Class<?> serviceClass,
			String version) {
		String interfaceName = serviceClass.getName();
		ReferenceBean<GenericService> referenceBean = build(interfaceName, version,
				serviceName, emptyMap());
		return referenceBean.get();
	}

	private ReferenceBean<GenericService> build(ServiceRestMetadata serviceRestMetadata,
			Map<String, Object> dubboTranslatedAttributes) {
		String urlValue = serviceRestMetadata.getUrl();
		URL url = URL.valueOf(urlValue);
		String interfaceName = url.getServiceInterface();
		String version = url.getParameter(VERSION_KEY);
		String group = url.getParameter(GROUP_KEY);

		return build(interfaceName, version, group, dubboTranslatedAttributes);
	}

	private ReferenceBean<GenericService> build(String interfaceName, String version,
			String group, Map<String, Object> dubboTranslatedAttributes) {

		Integer key = Objects.hash(interfaceName, version, group,
				dubboTranslatedAttributes);

		return cache.computeIfAbsent(group + key, k -> {
			ReferenceBean<GenericService> referenceBean = new ReferenceBean<>();
			referenceBean.setGeneric(true);
			referenceBean.setInterface(interfaceName);
			referenceBean.setVersion(version);
			referenceBean.setGroup(group);
			referenceBean.setCheck(false);
			bindReferenceBean(referenceBean, dubboTranslatedAttributes);
			return referenceBean;
		});
	}

	private void bindReferenceBean(ReferenceBean<GenericService> referenceBean,
			Map<String, Object> dubboTranslatedAttributes) {
		DataBinder dataBinder = new DataBinder(referenceBean);
		// Register CustomEditors for special fields
		dataBinder.registerCustomEditor(String.class, "filter",
				new StringTrimmerEditor(true));
		dataBinder.registerCustomEditor(String.class, "listener",
				new StringTrimmerEditor(true));
		dataBinder.registerCustomEditor(Map.class, "parameters",
				new PropertyEditorSupport() {

					@Override
					public void setAsText(String text)
							throws java.lang.IllegalArgumentException {
						// Trim all whitespace
						String content = StringUtils.trimAllWhitespace(text);
						if (!StringUtils.hasText(content)) { // No content , ignore
																// directly
							return;
						}
						// replace "=" to ","
						content = StringUtils.replace(content, "=", ",");
						// replace ":" to ","
						content = StringUtils.replace(content, ":", ",");
						// String[] to Map
						Map<String, String> parameters = CollectionUtils
								.toStringMap(commaDelimitedListToStringArray(content));
						setValue(parameters);
					}
				});

		// ignore "registries" field and then use RegistryConfig beans
		dataBinder.setDisallowedFields("registries");

		dataBinder.bind(new MutablePropertyValues(dubboTranslatedAttributes));

		registryConfigs.ifAvailable(referenceBean::setRegistries);
	}

	@PreDestroy
	public void destroy() {
		destroyReferenceBeans();
		cache.clear();
	}

	public synchronized void destroy(String serviceName) {
		Set<String> removeGroups = new HashSet<>(cache.keySet());
		for (String key : removeGroups) {
			if (key.contains(serviceName)) {
				ReferenceBean<GenericService> referenceBean = cache.remove(key);
				referenceBean.destroy();
			}
		}
	}

	private void destroyReferenceBeans() {
		Collection<ReferenceBean<GenericService>> referenceBeans = cache.values();
		if (logger.isInfoEnabled()) {
			logger.info("The Dubbo GenericService ReferenceBeans are destroying...");
		}
		for (ReferenceBean referenceBean : referenceBeans) {
			referenceBean.destroy(); // destroy ReferenceBean
			if (logger.isInfoEnabled()) {
				logger.info("Destroyed the ReferenceBean  : {} ", referenceBean);
			}
		}
	}

}
