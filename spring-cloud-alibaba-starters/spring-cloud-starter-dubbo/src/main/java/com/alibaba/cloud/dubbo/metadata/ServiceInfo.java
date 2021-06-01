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

package com.alibaba.cloud.dubbo.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.compiler.support.ClassUtils;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.ArrayUtils;
import org.apache.dubbo.common.utils.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.DOT_SEPARATOR;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_CHAR_SEPARATOR;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.METHODS_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;

/**
 * Copy from org.apache.dubbo.metadata.MetadataInfo.ServiceInfo.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ServiceInfo implements Serializable {

	private static final long serialVersionUID = -258557978718735302L;

	private static ExtensionLoader<MetadataParamsFilter> loader = ExtensionLoader
			.getExtensionLoader(MetadataParamsFilter.class);

	private String name;

	private String group;

	private String version;

	private String protocol;

	private String path; // most of the time, path is the same with the interface name.

	private Map<String, String> params;

	// params configured on consumer side,
	private transient Map<String, String> consumerParams;

	// cached method params
	private transient Map<String, Map<String, String>> methodParams;

	private transient Map<String, Map<String, String>> consumerMethodParams;

	// cached numbers
	private transient Map<String, Number> numbers;

	private transient Map<String, Map<String, Number>> methodNumbers;

	// service + group + version
	private transient String serviceKey;

	// service + group + version + protocol
	private transient String matchKey;

	private transient URL url;

	public ServiceInfo() {
	}

	public ServiceInfo(URL url) {
		this(url.getServiceInterface(), url.getParameter(GROUP_KEY),
				url.getParameter(VERSION_KEY), url.getProtocol(), url.getPath(), null);

		this.url = url;
		Map<String, String> params = new HashMap<>();
		List<MetadataParamsFilter> filters = loader.getActivateExtension(url,
				"params-filter");
		for (MetadataParamsFilter filter : filters) {
			String[] paramsIncluded = filter.serviceParamsIncluded();
			if (ArrayUtils.isNotEmpty(paramsIncluded)) {
				for (String p : paramsIncluded) {
					String value = url.getParameter(p);
					if (StringUtils.isNotEmpty(value) && params.get(p) == null) {
						params.put(p, value);
					}
					String[] methods = url.getParameter(METHODS_KEY, (String[]) null);
					if (methods != null) {
						for (String method : methods) {
							String mValue = getMethodParameterStrict(url, method, p);
							if (StringUtils.isNotEmpty(mValue)) {
								params.put(method + DOT_SEPARATOR + p, mValue);
							}
						}
					}
				}
			}
		}
		this.params = params;
	}

	public String getMethodParameterStrict(URL url, String method, String key) {
		Map<String, String> keyMap = url.getMethodParameters().get(method);
		String value = null;
		if (keyMap != null) {
			value = keyMap.get(key);
		}
		return value;
	}

	public ServiceInfo(String name, String group, String version, String protocol,
			String path, Map<String, String> params) {
		this.name = name;
		this.group = group;
		this.version = version;
		this.protocol = protocol;
		this.path = path;
		this.params = params == null ? new HashMap<>() : params;

		this.serviceKey = URL.buildKey(name, group, version);
		this.matchKey = buildMatchKey();
	}

	public String getMatchKey() {
		if (matchKey != null) {
			return matchKey;
		}
		buildMatchKey();
		return matchKey;
	}

	private String buildMatchKey() {
		matchKey = getServiceKey();
		if (StringUtils.isNotEmpty(protocol)) {
			matchKey = getServiceKey() + GROUP_CHAR_SEPARATOR + protocol;
		}
		return matchKey;
	}

	public String getServiceKey() {
		if (serviceKey != null) {
			return serviceKey;
		}
		this.serviceKey = URL.buildKey(name, group, version);
		return serviceKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getParams() {
		if (params == null) {
			return Collections.emptyMap();
		}
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Map<String, String> getAllParams() {
		if (consumerParams != null) {
			Map<String, String> allParams = new HashMap<>(
					(int) ((params.size() + consumerParams.size()) / 0.75f + 1));
			allParams.putAll(params);
			allParams.putAll(consumerParams);
			return allParams;
		}
		return params;
	}

	public String getParameter(String key) {
		if (consumerParams != null) {
			String value = consumerParams.get(key);
			if (value != null) {
				return value;
			}
		}
		return params.get(key);
	}

	public String getMethodParameter(String method, String key, String defaultValue) {
		if (methodParams == null) {
			methodParams = URL.toMethodParameters(params);
			consumerMethodParams = URL.toMethodParameters(consumerParams);
		}

		String value = getMethodParameter(method, key, consumerMethodParams);
		if (value != null) {
			return value;
		}
		value = getMethodParameter(method, key, methodParams);
		return value == null ? defaultValue : value;
	}

	private String getMethodParameter(String method, String key,
			Map<String, Map<String, String>> map) {
		Map<String, String> keyMap = map.get(method);
		String value = null;
		if (keyMap != null) {
			value = keyMap.get(key);
		}
		if (StringUtils.isEmpty(value)) {
			value = getParameter(key);
		}
		return value;
	}

	public boolean hasMethodParameter(String method, String key) {
		String value = this.getMethodParameter(method, key, (String) null);
		return StringUtils.isNotEmpty(value);
	}

	public boolean hasMethodParameter(String method) {
		if (methodParams == null) {
			methodParams = URL.toMethodParameters(params);
			consumerMethodParams = URL.toMethodParameters(consumerParams);
		}

		return consumerMethodParams.containsKey(method)
				|| methodParams.containsKey(method);
	}

	public String toDescString() {
		return this.getMatchKey() + getMethodSignaturesString() + getParams();
	}

	private String getMethodSignaturesString() {
		SortedSet<String> methodStrings = new TreeSet();

		Method[] methods = ClassUtils.forName(name).getMethods();
		for (Method method : methods) {
			methodStrings.add(method.toString());
		}
		return methodStrings.toString();
	}

	public void addParameter(String key, String value) {
		if (consumerParams != null) {
			this.consumerParams.put(key, value);
		}
	}

	public void addParameterIfAbsent(String key, String value) {
		if (consumerParams != null) {
			this.consumerParams.putIfAbsent(key, value);
		}
	}

	public void addConsumerParams(Map<String, String> params) {
		// copy once for one service subscription
		if (consumerParams == null) {
			consumerParams = new HashMap<>(params);
		}
	}

	public Map<String, Number> getNumbers() {
		// concurrent initialization is tolerant
		if (numbers == null) {
			numbers = new ConcurrentHashMap<>();
		}
		return numbers;
	}

	public Map<String, Map<String, Number>> getMethodNumbers() {
		if (methodNumbers == null) { // concurrent initialization is tolerant
			methodNumbers = new ConcurrentHashMap<>();
		}
		return methodNumbers;
	}

	public URL getUrl() {
		return url;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ServiceInfo)) {
			return false;
		}

		ServiceInfo serviceInfo = (ServiceInfo) obj;
		return this.getMatchKey().equals(serviceInfo.getMatchKey())
				&& this.getParams().equals(serviceInfo.getParams());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getMatchKey(), getParams());
	}

	@Override
	public String toString() {
		return "service{" + "name='" + name + "'," + "group='" + group + "',"
				+ "version='" + version + "'," + "protocol='" + protocol + "',"
				+ "params=" + params + "," + "consumerParams=" + consumerParams + "}";
	}

}
