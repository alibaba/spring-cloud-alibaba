/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.rpc.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.rpc.RpcProperties;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.ExchangeClient;
import org.apache.dubbo.remoting.exchange.Exchangers;
import org.apache.dubbo.remoting.exchange.support.ExchangeHandlerDispatcher;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

/**
 * @author :Lictory
 * @date : 2024/08/13
 */

public class UrlResolver {

	private static final ConcurrentHashMap<String, ExchangeClient> clientMap = new ConcurrentHashMap<>();

	public String resolveOriginalUrl(LoadBalancerClient loadBalancerClient, String url) {
		URL result = null;
		try {
			result = new URL(url);
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		String serviceId = result.getHost();
		ServiceInstance choose = loadBalancerClient.choose(serviceId);
		int nettyPort = Integer.parseInt(choose.getMetadata().get(RpcProperties.NETTY_PORT_PREFIX));
		return "exchange://" + choose.getHost() + ":" + nettyPort;
	}

	private void initLocalClient(String url) {

		org.apache.dubbo.common.URL targetUrl = org.apache.dubbo.common.URL.valueOf(url);
		try {
			clientMap.put(url, Exchangers.connect(targetUrl, new ExchangeHandlerDispatcher() {
						@Override
						public void received(Channel channel, Object message) {
							super.received(channel, message);
						}
					}
			));
		}
		catch (RemotingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getPathFromUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			StringBuilder path = new StringBuilder();
			path.append(url.getPath());
			if (url.getQuery() != null) {
				path.append("?").append(url.getQuery());
			}
			if (url.getRef() != null) {
				path.append("#").append(url.getRef());
			}
			return path.toString();
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL: " + urlString, e);
		}
	}

	public ExchangeClient getClient(String url) {
		if (url == null || url.length() == 0) {
			return null;
		}
		if (!clientMap.containsKey(url)) {
			initLocalClient(url);
		}
		return clientMap.get(url);
	}
}
