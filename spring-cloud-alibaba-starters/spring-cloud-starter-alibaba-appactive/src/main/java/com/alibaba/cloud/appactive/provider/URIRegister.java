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
				hasWildChar = collectServiceMetas(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.UNIT_RESOURCE_TYPE);
			} else if (filter instanceof GlobalServiceFilter) {
				hasWildChar = collectServiceMetas(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.CENTER_RESOURCE_TYPE);
			} else if (filter instanceof GeneralServiceFilter) {
				hasWildChar = collectServiceMetas(serviceMetaList, hasWildChar, urlPatterns,
						ResourceActiveType.NORMAL_RESOURCE_TYPE);
			}
		}
		if (CollectionUtils.isEmpty(serviceMetaList)) {
			return;
		}
		if (!hasWildChar) {
			// 保证所有 service(app+uri) 都纳入管理，不然不好做缓存管理
			collectServiceMeta(serviceMetaList, MATCH_ALL, ResourceActiveType.NORMAL_RESOURCE_TYPE);
		}
		initServiceMetaObject(serviceMetaList);
	}

	/**
	 * Initialize {@link #serviceMetaObject} based on {@link ServiceMeta} list.
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
	 * Collect {@link ServiceMeta} into the given <i>serviceMetaList</i> according to
	 * each item of the given <i>urlPatterns</i> and the given <i>resourceActiveType</i>,
	 * finally determine whether <i>hasWildChar</i> is a new wildChar.
	 * @param serviceMetaList extended list
	 * @param hasWildChar keyword to be determined
	 * @param urlPatterns looped list
	 * @param resourceActiveType attribute of {@link ServiceMeta}
	 * @return is new wildChar
	 */
	private static boolean collectServiceMetas(List<ServiceMeta> serviceMetaList, boolean hasWildChar,
										 Collection<String> urlPatterns, String resourceActiveType) {
		for (String urlPattern : urlPatterns) {
			if (MATCH_ALL.equalsIgnoreCase(urlPattern)) {
				hasWildChar = true;
			}
			collectServiceMeta(serviceMetaList, urlPattern, resourceActiveType);
		}
		return hasWildChar;
	}

	/**
	 * Collect {@link ServiceMeta} into the given <i>serviceMetaList</i> according to
	 * the given <i>urlPattern</i> and the given <i>resourceActiveType</i>.
	 * @param serviceMetaList extended list
	 * @param urlPattern attribute of {@link ServiceMeta}
	 * @param resourceActiveType attribute of {@link ServiceMeta}
	 */
	private static void collectServiceMeta(List<ServiceMeta> serviceMetaList, String urlPattern,
									   String resourceActiveType) {
		ServiceMeta serviceMeta = new ServiceMeta(urlPattern, resourceActiveType);
		serviceMetaList.add(serviceMeta);
	}

	public static ServiceMetaObject getServiceMetaObject() {
		return serviceMetaObject;
	}

}
