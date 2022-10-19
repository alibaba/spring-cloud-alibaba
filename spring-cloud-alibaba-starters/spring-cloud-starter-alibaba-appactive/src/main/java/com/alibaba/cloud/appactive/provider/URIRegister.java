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

package com.alibaba.cloud.appactive.provider;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Filter;

import com.alibaba.cloud.appactive.common.ServiceMeta;
import com.alibaba.cloud.appactive.common.ServiceMetaObject;
import com.alibaba.fastjson.JSON;
import io.appactive.java.api.base.constants.ResourceActiveType;
import io.appactive.support.lang.CollectionUtils;
import org.apache.commons.codec.digest.DigestUtils;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 * @author <a href="mailto:1262917629@qq.com">RovingSea</a>
 */
public final class URIRegister {

	private static final String MATCH_ALL = "/**";

	private static ServiceMetaObject serviceMetaObject;

	private URIRegister() {
	}

	public static void collectUris(List<FilterRegistrationBean<? extends Filter>> beanList) {
		if (CollectionUtils.isEmpty(beanList)) {
			return;
		}
		List<ServiceMeta> serviceMetaList = new LinkedList<>();
		boolean hasWildChar = false;
		for (FilterRegistrationBean<? extends Filter> filterRegistrationBean : beanList) {
			Filter filter = filterRegistrationBean.getFilter();
			if (filter == null) {
				continue;
			}
			Collection<String> urlPatterns = filterRegistrationBean.getUrlPatterns();
			if (filter instanceof CoreServiceFilter) {
				hasWildChar = expandAndDetermine(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.UNIT_RESOURCE_TYPE);
			} else if (filter instanceof GlobalServiceFilter) {
				hasWildChar = expandAndDetermine(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.CENTER_RESOURCE_TYPE);
			} else if (filter instanceof GeneralServiceFilter) {
				hasWildChar = expandAndDetermine(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.NORMAL_RESOURCE_TYPE);
			}
		}
		if (CollectionUtils.isEmpty(serviceMetaList)) {
			return;
		}
		if (!hasWildChar) {
			// 保证所有 service(app+uri) 都纳入管理，不然不好做缓存管理
			expand(serviceMetaList, MATCH_ALL, ResourceActiveType.NORMAL_RESOURCE_TYPE);
		}
		initServiceMetaObject(serviceMetaList);
	}

	/**
	 * initialize {@link #serviceMetaObject} based on {@link ServiceMeta} list.
	 * @param serviceMetaList list needed for initialization
	 */
	private static void initServiceMetaObject(List<ServiceMeta> serviceMetaList) {
		serviceMetaObject = new ServiceMetaObject();
		Collections.sort(serviceMetaList);
		serviceMetaObject.setServiceMetaList(serviceMetaList);
		String meta = JSON.toJSONString(serviceMetaList);
		serviceMetaObject.setMeta(meta);
		String md5 = DigestUtils.md5Hex(meta.getBytes(StandardCharsets.UTF_8));
		serviceMetaObject.setMd5OfList(md5);
	}

	/**
	 * Expand {@link ServiceMeta} list while determining whether it is a wild char.
	 * @param serviceMetaList extended list
	 * @param hasWildChar keyword to be determined
	 * @param urlPatterns looped list
	 * @param resourceActiveType attribute of {@link ServiceMeta}
	 * @return is that a wild char
	 */
	private static boolean expandAndDetermine(List<ServiceMeta> serviceMetaList, boolean hasWildChar,
										 Collection<String> urlPatterns, String resourceActiveType) {
		for (String urlPattern : urlPatterns) {
			if (MATCH_ALL.equalsIgnoreCase(urlPattern)) {
				hasWildChar = true;
			}
			expand(serviceMetaList, urlPattern, resourceActiveType);
		}
		return hasWildChar;
	}

	/**
	 * Expand ServiceMeta list.
	 * @param serviceMetaList extended list
	 * @param urlPattern attribute of {@link ServiceMeta}
	 * @param resourceActiveType attribute of {@link ServiceMeta}
	 */
	private static void expand(List<ServiceMeta> serviceMetaList, String urlPattern,
									   String resourceActiveType) {
		ServiceMeta serviceMeta = new ServiceMeta(urlPattern, resourceActiveType);
		serviceMetaList.add(serviceMeta);
	}

	public static ServiceMetaObject getServiceMetaObject() {
		return serviceMetaObject;
	}

}
