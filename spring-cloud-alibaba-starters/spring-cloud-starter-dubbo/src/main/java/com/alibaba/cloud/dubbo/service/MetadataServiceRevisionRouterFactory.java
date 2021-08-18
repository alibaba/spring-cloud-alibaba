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

package com.alibaba.cloud.dubbo.service;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Router;
import org.apache.dubbo.rpc.cluster.RouterFactory;
import org.apache.dubbo.rpc.cluster.router.AbstractRouter;

import org.springframework.util.CollectionUtils;

import static com.alibaba.cloud.dubbo.metadata.RevisionResolver.SCA_REVSION_KEY;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MetadataServiceRevisionRouterFactory implements RouterFactory {

	@Override
	public Router getRouter(URL url) {
		return new AbstractRouter() {
			@Override
			public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url,
					Invocation invocation) throws RpcException {
				if (CollectionUtils.isEmpty(invokers)) {
					return invokers;
				}

				if (!DubboMetadataService.class.getName()
						.equalsIgnoreCase(url.getServiceInterface())) {
					return invokers;
				}

				String revision = invocation.getAttachment(SCA_REVSION_KEY);

				if (StringUtils.isEmpty(revision)) {
					return invokers;
				}

				List<Invoker<T>> list = new ArrayList<>(invokers.size());

				for (Invoker<T> invoker : invokers) {
					if (StringUtils.equals(revision,
							invoker.getUrl().getParameter(SCA_REVSION_KEY))) {
						list.add(invoker);
					}
				}

				return list;
			}
		};
	}

}
