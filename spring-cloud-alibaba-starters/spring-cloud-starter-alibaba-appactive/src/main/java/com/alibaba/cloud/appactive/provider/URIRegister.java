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
 */
public class URIRegister {

	private static final String MATCH_ALL = "/**";

	private static ServiceMetaObject serviceMetaObject;

	/**
	 * publish to registry meta like dubbo rather than building a new appactive rule type
	 */
	public static void collectUris(List<FilterRegistrationBean> beanList) {
		if (CollectionUtils.isNotEmpty(beanList)) {
			List<ServiceMeta> serviceMetaList = new LinkedList<>();
			boolean hasWildChar = false;
			for (FilterRegistrationBean filterRegistrationBean : beanList) {
				Filter filter = filterRegistrationBean.getFilter();
				if (filter == null) {
					continue;
				}
				if (filter instanceof UnitServiceFilter) {
					Collection<String> urlPatterns = filterRegistrationBean
							.getUrlPatterns();
					for (String urlPattern : urlPatterns) {
						if (MATCH_ALL.equalsIgnoreCase(urlPattern)) {
							hasWildChar = true;
						}
						ServiceMeta serviceMeta = new ServiceMeta(urlPattern,
								ResourceActiveType.UNIT_RESOURCE_TYPE);
						serviceMetaList.add(serviceMeta);
					}
				}
				else if (filter instanceof CenterServiceFilter) {
					Collection<String> urlPatterns = filterRegistrationBean
							.getUrlPatterns();
					for (String urlPattern : urlPatterns) {
						if (MATCH_ALL.equalsIgnoreCase(urlPattern)) {
							hasWildChar = true;
						}
						ServiceMeta serviceMeta = new ServiceMeta(urlPattern,
								ResourceActiveType.CENTER_RESOURCE_TYPE);
						serviceMetaList.add(serviceMeta);
					}
				}
				else if (filter instanceof NormalServiceFilter) {
					Collection<String> urlPatterns = filterRegistrationBean
							.getUrlPatterns();
					for (String urlPattern : urlPatterns) {
						if (MATCH_ALL.equalsIgnoreCase(urlPattern)) {
							hasWildChar = true;
						}
						ServiceMeta serviceMeta = new ServiceMeta(urlPattern,
								ResourceActiveType.NORMAL_RESOURCE_TYPE);
						serviceMetaList.add(serviceMeta);
					}
				}
			}
			if (CollectionUtils.isNotEmpty(serviceMetaList)) {
				if (!hasWildChar) {
					// 保证所有 service(app+uri) 都纳入管理，不然不好做缓存管理
					ServiceMeta serviceMeta = new ServiceMeta(MATCH_ALL,
							ResourceActiveType.NORMAL_RESOURCE_TYPE);
					serviceMetaList.add(serviceMeta);
				}
				serviceMetaObject = new ServiceMetaObject();
				Collections.sort(serviceMetaList);
				serviceMetaObject.setServiceMetaList(serviceMetaList);
				String meta = JSON.toJSONString(serviceMetaList);
				serviceMetaObject.setMeta(meta);
				String md5 = DigestUtils.md5Hex(meta.getBytes(StandardCharsets.UTF_8));
				serviceMetaObject.setMd5OfList(md5);
			}
		}
	}

	public static ServiceMetaObject getServiceMetaObject() {
		return serviceMetaObject;
	}

}
