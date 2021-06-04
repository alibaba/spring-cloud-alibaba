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

/**
 * @author <a href="a11en.huang@foxmail.com">Allen Huang</a>
 */
public final class ResourceHandlerHolder {

	/**
	 * 基于rest api的策略类型.每一个url都会被当作一个资源.
	 */
	public static final String REST_API = "rest-api";

	/**
	 * 基于服务的策略类型.一个服务只有一个资源.
	 */
	public static final String SERVICE_INSTANCE = "service-instance";

	private ResourceHandlerHolder() {

	}

}
