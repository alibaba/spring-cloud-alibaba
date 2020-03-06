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

package com.alibaba.cloud.nacos.ribbon;

import java.util.Map;

import com.netflix.loadbalancer.Server;

import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

/**
 * @author xiaojing
 */
public class NacosServerIntrospector extends DefaultServerIntrospector {

	@Override
	public Map<String, String> getMetadata(Server server) {
		if (server instanceof NacosServer) {
			return ((NacosServer) server).getMetadata();
		}
		return super.getMetadata(server);
	}

	@Override
	public boolean isSecure(Server server) {
		if (server instanceof NacosServer) {
			return Boolean.valueOf(((NacosServer) server).getMetadata().get("secure"));
		}

		return super.isSecure(server);
	}

}
