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

package com.alibaba.cloud.opensergo.description;

import java.util.Map;

import io.opensergo.proto.service_contract.v1.MethodDescriptor;
import io.opensergo.proto.service_contract.v1.ServiceDescriptor;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

public final class RequestMappingInfoHandlerMappingDescriptionProvider
		implements HandlerMappingDescriptionProvider<RequestMappingInfoHandlerMapping> {

	@Override
	public Class<RequestMappingInfoHandlerMapping> getMappingClass() {
		return RequestMappingInfoHandlerMapping.class;
	}

	@Override
	public void process(RequestMappingInfoHandlerMapping handlerMapping,
			ServiceDescriptor.Builder serviceBuilder) {
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping
				.getHandlerMethods();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods
				.entrySet()) {
			RequestMappingInfo k = entry.getKey();
			HandlerMethod v = entry.getValue();

			MethodDescriptor.Builder builder = MethodDescriptor.newBuilder();

			if (k.getPathPatternsCondition() != null) {
				for (PathPattern pattern : k.getPathPatternsCondition().getPatterns()) {
					builder.addHttpPaths(pattern.getPatternString());
				}
			}
			for (RequestMethod method : k.getMethodsCondition().getMethods()) {
				builder.addHttpMethods(method.name());
			}
			serviceBuilder.addMethods(builder.build());
		}
	}
}
