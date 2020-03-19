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

package com.alibaba.cloud.dubbo.metadata.resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.metadata.ServiceRestMetadata;
import feign.Contract;
import feign.Feign;
import feign.MethodMetadata;
import feign.Util;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.spring.ServiceBean;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.ClassUtils;

/**
 * The metadata resolver for {@link Feign} for {@link ServiceBean Dubbo Service Bean} in
 * the provider side.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboServiceBeanMetadataResolver
		implements BeanClassLoaderAware, SmartInitializingSingleton, MetadataResolver {

	private static final String[] CONTRACT_CLASS_NAMES = { "feign.jaxrs2.JAXRS2Contract",
			"org.springframework.cloud.openfeign.support.SpringMvcContract", };

	private final ObjectProvider<Contract> contractObjectProvider;

	private ClassLoader classLoader;

	/**
	 * Feign Contracts.
	 */
	private Collection<Contract> contracts;

	public DubboServiceBeanMetadataResolver(
			ObjectProvider<Contract> contractObjectProvider) {
		this.contractObjectProvider = contractObjectProvider;
	}

	@Override
	public void afterSingletonsInstantiated() {

		LinkedList<Contract> contracts = new LinkedList<>();

		// Add injected Contract if available, for example SpringMvcContract Bean under
		// Spring Cloud Open Feign
		Contract contract = contractObjectProvider.getIfAvailable();

		if (contract != null) {
			contracts.add(contract);
		}

		Stream.of(CONTRACT_CLASS_NAMES).filter(this::isClassPresent) // filter the existed
																		// classes
				.map(this::loadContractClass) // load Contract Class
				.map(this::createContract) // createServiceInstance instance by the
											// specified class
				.forEach(contracts::add); // add the Contract instance into contracts

		this.contracts = Collections.unmodifiableCollection(contracts);
	}

	private Contract createContract(Class<?> contractClassName) {
		return (Contract) BeanUtils.instantiateClass(contractClassName);
	}

	private Class<?> loadContractClass(String contractClassName) {
		return ClassUtils.resolveClassName(contractClassName, classLoader);
	}

	private boolean isClassPresent(String className) {
		return ClassUtils.isPresent(className, classLoader);
	}

	@Override
	public Set<ServiceRestMetadata> resolveServiceRestMetadata(ServiceBean serviceBean) {

		Object bean = serviceBean.getRef();

		Class<?> beanType = bean.getClass();

		Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

		Set<RestMethodMetadata> methodRestMetadata = resolveMethodRestMetadata(beanType);

		List<URL> urls = serviceBean.getExportedUrls();

		urls.stream().map(URL::toString).forEach(url -> {
			ServiceRestMetadata metadata = new ServiceRestMetadata();
			metadata.setUrl(url);
			metadata.setMeta(methodRestMetadata);
			serviceRestMetadata.add(metadata);
		});

		return serviceRestMetadata;
	}

	@Override
	public Set<RestMethodMetadata> resolveMethodRestMetadata(Class<?> targetType) {
		List<Method> feignContractMethods = selectFeignContractMethods(targetType);
		return contracts.stream()
				.map(contract -> parseAndValidateMetadata(contract, targetType))
				.flatMap(v -> v.stream())
				.map(methodMetadata -> resolveMethodRestMetadata(methodMetadata,
						targetType, feignContractMethods))
				.collect(Collectors.toSet());
	}

	private List<MethodMetadata> parseAndValidateMetadata(Contract contract,
			Class<?> targetType) {
		List<MethodMetadata> methodMetadataList = Collections.emptyList();
		try {
			methodMetadataList = contract.parseAndValidateMetadata(targetType);
		}
		catch (Throwable ignored) {
			// ignore
		}
		return methodMetadataList;
	}

	/**
	 * Select feign contract methods
	 * <p>
	 * extract some code from
	 * {@link Contract.BaseContract#parseAndValidateMetadata(Class)}.
	 * @param targetType class of type
	 * @return non-null
	 */
	private List<Method> selectFeignContractMethods(Class<?> targetType) {
		List<Method> methods = new LinkedList<>();
		for (Method method : targetType.getMethods()) {
			if (method.getDeclaringClass() == Object.class
					|| (method.getModifiers() & Modifier.STATIC) != 0
					|| Util.isDefault(method)) {
				continue;
			}
			methods.add(method);
		}
		return methods;
	}

	protected RestMethodMetadata resolveMethodRestMetadata(MethodMetadata methodMetadata,
			Class<?> targetType, List<Method> feignContractMethods) {
		String configKey = methodMetadata.configKey();
		Method feignContractMethod = getMatchedFeignContractMethod(targetType,
				feignContractMethods, configKey);
		RestMethodMetadata metadata = new RestMethodMetadata(methodMetadata);
		metadata.setMethod(
				new com.alibaba.cloud.dubbo.metadata.MethodMetadata(feignContractMethod));
		return metadata;
	}

	private Method getMatchedFeignContractMethod(Class<?> targetType,
			List<Method> methods, String expectedConfigKey) {
		Method matchedMethod = null;
		for (Method method : methods) {
			String configKey = Feign.configKey(targetType, method);
			if (expectedConfigKey.equals(configKey)) {
				matchedMethod = method;
				break;
			}
		}
		return matchedMethod;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}
