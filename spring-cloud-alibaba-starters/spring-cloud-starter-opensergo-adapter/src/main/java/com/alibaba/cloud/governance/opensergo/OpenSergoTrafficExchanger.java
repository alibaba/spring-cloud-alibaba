/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.opensergo;

import java.util.Collection;
import java.util.List;

import com.alibaba.cloud.commons.governance.event.RoutingDataChangedEvent;
import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.opensergo.util.ConvUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.opensergo.ConfigKind;
import io.opensergo.OpenSergoClient;
import io.opensergo.subscribe.OpenSergoConfigSubscriber;
import io.opensergo.subscribe.SubscribeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * OpenSergoTrafficExchanger is the class which communicate with OpenSergo control plane.
 *
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
public class OpenSergoTrafficExchanger implements ApplicationContextAware {

	protected static final Logger log = LoggerFactory
			.getLogger(OpenSergoTrafficExchanger.class);

	private OpenSergoClient client;

	private ApplicationContext applicationContext;

	private OpenSergoTrafficRouterParser openSergoTrafficRouterParser;

	public OpenSergoTrafficExchanger(OpenSergoConfigProperties openSergoConfigProperties,
			OpenSergoTrafficRouterParser openSergoTrafficRouterParser) {
		Integer port = ConvUtils
				.getOpenSergoPort(openSergoConfigProperties.getEndpoint());
		String host = ConvUtils.getOpenSergoHost(openSergoConfigProperties.getEndpoint());

		this.openSergoTrafficRouterParser = openSergoTrafficRouterParser;
		try {
			if (port != null && StringUtils.isNotEmpty(host)) {
				client = new OpenSergoClient(host, port);
				client.start();
			}
			else {
				log.error("OpenSergo endpoint：" + openSergoConfigProperties.getEndpoint()
						+ " is illegal");
			}
		}
		catch (Exception e) {
			log.error("start OpenSergo client enhance error", e);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void subscribeTrafficRouterConfig(String namespace, String appName) {
		client.subscribeConfig(
				new SubscribeKey(namespace, appName, ConfigKind.TRAFFIC_ROUTER_STRATEGY),
				new OpenSergoConfigSubscriber() {
					@Override
					public boolean onConfigUpdate(SubscribeKey subscribeKey,
							Object dataList) {
						log.debug("OpenSergo client subscribeKey:{} receive message :{}",
								subscribeKey, dataList);
						try {
							Collection<UnifiedRoutingDataStructure> rules = openSergoTrafficRouterParser
									.resolveLabelRouting(
											(List<RouteConfiguration>) dataList);
							applicationContext.publishEvent(
									new RoutingDataChangedEvent(this, rules));
						}
						catch (InvalidProtocolBufferException e) {
							log.error("resolve label routing enhance error", e);
							return false;
						}
						return true;
					}
				});
	}

}
