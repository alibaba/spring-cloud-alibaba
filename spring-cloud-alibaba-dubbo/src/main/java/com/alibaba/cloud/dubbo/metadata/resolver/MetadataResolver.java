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

import java.util.Set;

import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.metadata.ServiceRestMetadata;
import org.apache.dubbo.config.spring.ServiceBean;

/**
 * The REST metadata resolver.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public interface MetadataResolver {

	/**
	 * Resolve the {@link ServiceRestMetadata} {@link Set set} from {@link ServiceBean}.
	 * @param serviceBean {@link ServiceBean}
	 * @return non-null {@link Set}
	 */
	Set<ServiceRestMetadata> resolveServiceRestMetadata(ServiceBean serviceBean);

	/**
	 * Resolve {@link RestMethodMetadata} {@link Set set} from {@link Class target type}.
	 * @param targetType {@link Class target type}
	 * @return non-null {@link Set}
	 */
	Set<RestMethodMetadata> resolveMethodRestMetadata(Class<?> targetType);

}
