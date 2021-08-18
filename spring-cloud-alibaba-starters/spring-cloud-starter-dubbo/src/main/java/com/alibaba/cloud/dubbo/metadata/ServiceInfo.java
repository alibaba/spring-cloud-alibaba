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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.compiler.support.ClassUtils;
import org.apache.dubbo.common.utils.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.GROUP_CHAR_SEPARATOR;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.INTERFACE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.METHODS_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PID_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.TIMESTAMP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;

/**
 * Copy from org.apache.dubbo.metadata.MetadataInfo.ServiceInfo.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ServiceInfo implements Serializable {

	private static final long serialVersionUID = -258557978718735302L;

	private String name;

	private String group;

	private String version;

	private String protocol;

	private String path; // most of the time, path is the same with the interface name.

	private Map<String, String> params;

	// params configured on consumer side,
	private transient Map<String, String> consumerParams;

	// service + group + version
	private transient String serviceKey;

	// service + group + version + protocol
	private transient String matchKey;

	private transient URL url;

	private static final Set<String> IGNORE_KEYS = new HashSet<>();
	static {
		IGNORE_KEYS.add(TIMESTAMP_KEY);
		IGNORE_KEYS.add(PID_KEY);
		IGNORE_KEYS.add(INTERFACE_KEY);
		IGNORE_KEYS.add(METHODS_KEY);
	}

	public ServiceInfo(URL url) {
		this(url.getServiceInterface(), url.getParameter(GROUP_KEY),
				url.getParameter(VERSION_KEY), url.getProtocol(), url.getPath(), null);

		this.url = url;
		Map<String, String> params = new TreeMap<>();
		url.getParameters().forEach((k, v) -> {
			if (IGNORE_KEYS.contains(k)) {
				return;
			}
			params.put(k, v);
		});
		this.params = params;
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

	public String getParameter(String key) {
		if (consumerParams != null) {
			String value = consumerParams.get(key);
			if (value != null) {
				return value;
			}
		}
		return params.get(key);
	}

	public String toDescString() {
		return this.getMatchKey() + getMethodSignaturesString() + getParams();
	}

	private String getMethodSignaturesString() {
		SortedSet<String> methodStrings = new TreeSet<>();

		Method[] methods = ClassUtils.forName(name).getMethods();
		for (Method method : methods) {
			methodStrings.add(method.toString());
		}
		return methodStrings.toString();
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
