/*
 * Copyright (C) 2018 the original author or authors.
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

import static java.lang.reflect.Proxy.newProxyInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * The proxy of {@link DubboMetadataService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboMetadataServiceProxy implements BeanClassLoaderAware, DisposableBean {

	private final DubboGenericServiceFactory dubboGenericServiceFactory;
	private final Map<String, DubboMetadataService> dubboMetadataServiceCache = new ConcurrentHashMap<>();
	private ClassLoader classLoader;

	public DubboMetadataServiceProxy(
			DubboGenericServiceFactory dubboGenericServiceFactory) {
		this.dubboGenericServiceFactory = dubboGenericServiceFactory;
	}

	/**
	 * Initializes {@link DubboMetadataService}'s Proxy
	 *
	 * @param serviceName the service name
	 * @param version the service version
	 * @return a {@link DubboMetadataService} proxy
	 */
	public DubboMetadataService initProxy(String serviceName, String version) {
		return dubboMetadataServiceCache.computeIfAbsent(serviceName,
				name -> newProxy(name, version));
	}

	/**
	 * Remove {@link DubboMetadataService}'s Proxy by service name
	 * @param serviceName the service name
	 */
	public void removeProxy(String serviceName) {
		dubboMetadataServiceCache.remove(serviceName);
	}

	/**
	 * Get a proxy instance of {@link DubboMetadataService} via the specified service name
	 *
	 * @param serviceName the service name
	 * @return a {@link DubboMetadataService} proxy
	 */
	public DubboMetadataService getProxy(String serviceName) {
		return dubboMetadataServiceCache.get(serviceName);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void destroy() throws Exception {
		dubboMetadataServiceCache.clear();
	}

	/**
	 * New a proxy instance of {@link DubboMetadataService} via the specified service name
	 *
	 * @param serviceName the service name
	 * @param version the service version
	 * @return a {@link DubboMetadataService} proxy
	 */
	protected DubboMetadataService newProxy(String serviceName, String version) {
		return (DubboMetadataService) newProxyInstance(classLoader,
				new Class[] { DubboMetadataService.class },
				new DubboMetadataServiceInvocationHandler(serviceName, version,
						dubboGenericServiceFactory));
	}
}
