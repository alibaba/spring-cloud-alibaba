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

package com.alibaba.cloud.example.common.service.strategy;

import java.util.List;

import com.alibaba.cloud.example.common.ProductService;
import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author yuluo
 */

@Component
public class WebClientProductService implements ProductService {

	@Autowired(required = false)
	WebClient webClient;

	@Autowired
	private FeignProductService feignProductService;

	@Override
	public ResultHolder<List<Product>> list() {
		if (webClient != null) {
			Mono<ResultHolder> resultHolderMono = webClient.get()
					.uri("http://product/list").retrieve().bodyToMono(ResultHolder.class);
			return resultHolderMono.block();
		}
		return feignProductService.list();
	}

	@Override
	public ResultHolder<Product> detail(String rId, String pId) {

		if (webClient != null) {
			Mono<ResultHolder> resultHolderMono = webClient.get()
					.uri(uriBuilder -> uriBuilder.scheme("http").host("product")
							.path("/detail").queryParam("rId", rId).queryParam("pId", pId)
							.build())
					.retrieve().bodyToMono(ResultHolder.class);

			return resultHolderMono.block();
		}

		return feignProductService.detail(rId, pId);
	}

	@Override
	public ResultHolder<Product> detailHidden(String pId) {

		return feignProductService.detailHidden(pId);
	}

	@Override
	public ResultHolder<String> buy(String rpcType, String rId, String pId,
			Integer number) {

		return feignProductService.buy(rpcType, rId, pId, number);
	}

}
