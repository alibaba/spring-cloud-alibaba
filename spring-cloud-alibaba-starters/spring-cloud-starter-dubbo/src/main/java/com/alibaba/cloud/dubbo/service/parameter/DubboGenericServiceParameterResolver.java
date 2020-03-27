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

package com.alibaba.cloud.dubbo.service.parameter;

import com.alibaba.cloud.dubbo.http.HttpServerRequest;
import com.alibaba.cloud.dubbo.metadata.MethodParameterMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import org.apache.dubbo.rpc.service.GenericService;

import org.springframework.core.Ordered;

/**
 * Dubbo {@link GenericService} Parameter Resolver.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public interface DubboGenericServiceParameterResolver extends Ordered {

	/**
	 * Resolves a method parameter into an argument value from a given request.
	 * @param restMethodMetadata method request metadata
	 * @param methodParameterMetadata metadata of method
	 * @param request Http server request
	 * @return the result of resolve
	 */
	Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata, HttpServerRequest request);

	Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata,
			RestMethodMetadata clientRestMethodMetadata, Object[] arguments);

}
