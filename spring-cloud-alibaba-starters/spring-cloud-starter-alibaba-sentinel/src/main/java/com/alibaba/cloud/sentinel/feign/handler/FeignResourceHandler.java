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

package com.alibaba.cloud.sentinel.feign.handler;

import feign.MethodMetadata;
import feign.Target;

/**
 * @author <a href="a11en.huang@foxmail.com">Allen Huang</a>
 */
public interface FeignResourceHandler {

	/**
	 * create resource name by this strategy.
	 * @param target feign proxy target
	 * @param methodMetadata method metadata
	 * @return resource name
	 */
	String makeResourceName(Target<?> target, MethodMetadata methodMetadata);

	/**
	 * 此策略方法的类型。类型不能重复.
	 * @return 返回类型值
	 */
	String handlerType();

}
