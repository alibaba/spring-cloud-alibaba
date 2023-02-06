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

package com.alibaba.cloud.example.common.service;

import java.util.List;

import com.alibaba.cloud.example.common.RPCType;
import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;
import com.alibaba.cloud.example.common.service.strategy.FeignProductService;
import com.alibaba.cloud.example.common.service.strategy.RestTemplateProductService;
import com.alibaba.cloud.example.common.service.strategy.WebClientProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductDAO {

	@Autowired(required = false)
	RestTemplate restTemplate;

	@Autowired
	private FeignProductService feignRequest;

	@Autowired
	private RestTemplateProductService restTemplateRequest;

	@Autowired
	private WebClientProductService webClientRequest;

	public ResultHolder<List<Product>> list() {
		return feignRequest.list();
	}

	public ResultHolder<Product> detail(String rId, String pId) {
		return feignRequest.detail(rId, pId);
	}

	public ResultHolder<Product> detailHidden(String pId) {
		return feignRequest.detailHidden(pId);
	}

	public ResultHolder<String> buy(String rId, String pId, Integer number) {
		return feignRequest.buy(RPCType.SpringCloud.name(), rId, pId, number);
	}

	public ResultHolder<List<Product>> listTemplate() {

		return restTemplateRequest.list();
	}

	public ResultHolder<Product> detailTemplate(String rId, String pId) {

		return restTemplateRequest.detail(rId, pId);
	}

	public ResultHolder<List<Product>> listWebClient() {

		return webClientRequest.list();
	}

	public ResultHolder<Product> detailWebClient(String rId, String pId) {

		return webClientRequest.detail(rId, pId);
	}

}
