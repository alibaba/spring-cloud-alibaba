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

package com.alibaba.cloud.rpc.client;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.alibaba.cloud.rpc.metadata.HttpMetadata;
import com.alibaba.cloud.rpc.metadata.HttpRpcResponse;
import com.alibaba.cloud.rpc.utils.UrlResolver;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.ExchangeClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

/**
 * @author :Lictory
 * @date : 2024/08/12
 */

@Component
public class FeignRpcClient implements Client {
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	private ExchangeClient client;

	@Autowired
	private UrlResolver urlResolver;

	@Override
	public Response execute(Request request, Request.Options options) {
		String url = urlResolver.resolveOriginalUrl(loadBalancerClient, request.url());
		this.client = urlResolver.getClient(url);
		HttpMetadata httpMetadata = initHttpMetadata(
				urlResolver.getPathFromUrl(request.url()),
				request.httpMethod().name(),
				request.headers(),
				request.body()
		);
		HttpRpcResponse httpRpcResponse = null;
		try {
			CompletableFuture<Object> future = client.request(httpMetadata);
			httpRpcResponse = (HttpRpcResponse) future.get();
		}
		catch (RemotingException | InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		return Response.builder()
				.status(httpRpcResponse.getStatusCode())
				.reason(httpRpcResponse.getReasonPhrase())
				.headers(httpRpcResponse.getHeaders())
				.body(httpRpcResponse.getBody())
				.request(request)
				.build();
	}

	private HttpMetadata initHttpMetadata(String url, String method, Map<String, Collection<String>> headers, byte[] body) {
		HttpMetadata httpMetadata = new HttpMetadata();
		httpMetadata.setBody(body);
		httpMetadata.setUrl(url);
		httpMetadata.setHeaders(headers);
		httpMetadata.setMethod(method);
		return httpMetadata;
	}
}
