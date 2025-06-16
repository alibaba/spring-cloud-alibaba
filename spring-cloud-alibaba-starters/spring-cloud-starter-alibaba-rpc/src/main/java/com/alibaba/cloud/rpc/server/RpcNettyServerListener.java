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

package com.alibaba.cloud.rpc.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.alibaba.cloud.rpc.RpcProperties;
import com.alibaba.cloud.rpc.metadata.HttpMetadata;
import com.alibaba.cloud.rpc.metadata.HttpRpcResponse;
import com.alibaba.cloud.rpc.utils.MockHttpServletRequestConverter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.remoting.exchange.Exchangers;
import org.apache.dubbo.remoting.exchange.support.ExchangeHandlerAdapter;
import org.apache.dubbo.rpc.model.FrameworkModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author :Lictory
 * @date : 2024/08/18
 */

public class RpcNettyServerListener implements ApplicationListener<ApplicationReadyEvent> {
	private ExchangeServer server;

	@Autowired
	private RpcProperties rpcProperties;

	private RpcDispatcherServlet dispatcherServlet;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		initLocalServer(rpcProperties.getHost(), rpcProperties.getPort());
		this.dispatcherServlet = new RpcDispatcherServlet(applicationContext);
	}

	private void initLocalServer(String host, int port) {
		URL url = URL.valueOf("exchange://" + host + ":" + port);
		try {
			this.server = Exchangers.bind(url, new ExchangeHandlerAdapter(FrameworkModel.defaultModel()) {
						@Override
						public CompletableFuture<Object> reply(ExchangeChannel channel, Object msg) {
							HttpMetadata httpMetadata = (HttpMetadata) msg;
							String url = httpMetadata.getUrl();
							System.out.println(url);
							MockHttpServletRequest request = MockHttpServletRequestConverter.getMockHttpServletRequest(httpMetadata, url);
							MockHttpServletResponse response = new MockHttpServletResponse();
							try {
								dispatcherServlet.service(request, response);
							}
							catch (ServletException | IOException e) {
								throw new RuntimeException(e);
							}
							HttpRpcResponse httpRpcResponse = new HttpRpcResponse();
							httpRpcResponse.setStatusCode(response.getStatus());
							httpRpcResponse.setBody(response.getContentAsByteArray());
							httpRpcResponse.setReasonPhrase(response.getErrorMessage());
							httpRpcResponse.setHeaders(convertHeaders(response));
							return CompletableFuture.completedFuture(httpRpcResponse);
						}
					}
			);
		}
		catch (RemotingException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, Collection<String>> convertHeaders(HttpServletResponse httpServletResponse) {
		Map<String, Collection<String>> feignHeaders = new HashMap<>();
		// get all headers
		for (String headerName : httpServletResponse.getHeaderNames()) {
			Collection<String> headerValues = httpServletResponse.getHeaders(headerName);
			feignHeaders.put(headerName, new ArrayList<>(headerValues));
		}
		return feignHeaders;
	}
}



