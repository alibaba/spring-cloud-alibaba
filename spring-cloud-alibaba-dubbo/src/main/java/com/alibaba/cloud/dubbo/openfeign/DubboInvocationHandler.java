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
package com.alibaba.cloud.dubbo.openfeign;

import static org.apache.dubbo.common.utils.PojoUtils.realize;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.util.ClassUtils;

import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContext;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;

/**
 * Dubbo {@link GenericService} for {@link InvocationHandler}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboInvocationHandler implements InvocationHandler {

	private final Map<Method, FeignMethodMetadata> feignMethodMetadataMap;

	private final InvocationHandler defaultInvocationHandler;

	private final DubboGenericServiceExecutionContextFactory contextFactory;

	private final ClassLoader classLoader;

	public DubboInvocationHandler(Map<Method, FeignMethodMetadata> feignMethodMetadataMap,
			InvocationHandler defaultInvocationHandler, ClassLoader classLoader,
			DubboGenericServiceExecutionContextFactory contextFactory) {
		this.feignMethodMetadataMap = feignMethodMetadataMap;
		this.defaultInvocationHandler = defaultInvocationHandler;
		this.classLoader = classLoader;
		this.contextFactory = contextFactory;
	}

	@Override
	public Object invoke(Object proxy, Method feignMethod, Object[] args)
			throws Throwable {

		FeignMethodMetadata feignMethodMetadata = feignMethodMetadataMap.get(feignMethod);

		if (feignMethodMetadata == null) {
			return defaultInvocationHandler.invoke(proxy, feignMethod, args);
		}

		GenericService dubboGenericService = feignMethodMetadata.getDubboGenericService();
		RestMethodMetadata dubboRestMethodMetadata = feignMethodMetadata
				.getDubboRestMethodMetadata();
		RestMethodMetadata feignRestMethodMetadata = feignMethodMetadata
				.getFeignMethodMetadata();

		DubboGenericServiceExecutionContext context = contextFactory
				.create(dubboRestMethodMetadata, feignRestMethodMetadata, args);

		String methodName = context.getMethodName();
		String[] parameterTypes = context.getParameterTypes();
		Object[] parameters = context.getParameters();

		Object result = dubboGenericService.$invoke(methodName, parameterTypes,
				parameters);

		Class<?> returnType = getReturnType(dubboRestMethodMetadata);

		return realize(result, returnType);
	}

	private Class<?> getReturnType(RestMethodMetadata dubboRestMethodMetadata) {
		String returnType = dubboRestMethodMetadata.getReturnType();
		return ClassUtils.resolveClassName(returnType, classLoader);
	}
}
