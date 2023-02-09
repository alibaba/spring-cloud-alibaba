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
	private FeignProductService feignProductService;

	@Autowired
	private RestTemplateProductService restTemplateProductService;

	@Autowired
	private WebClientProductService webClientProductService;

	public ResultHolder<List<Product>> list() {
		return feignProductService.list();
	}

	public ResultHolder<Product> detail(String rId, String pId) {
		return feignProductService.detail(rId, pId);
	}

	public ResultHolder<Product> detailHidden(String pId) {
		return feignProductService.detailHidden(pId);
	}

	public ResultHolder<String> buy(String rId, String pId, Integer number) {
		return feignProductService.buy(RPCType.SpringCloud.name(), rId, pId, number);
	}

	public ResultHolder<List<Product>> listByRestTemplate() {

		return restTemplateProductService.list();
	}

	public ResultHolder<Product> detailByRestTemplate(String rId, String pId) {

		return restTemplateProductService.detail(rId, pId);
	}

	public ResultHolder<List<Product>> listByWebClient() {

		return webClientProductService.list();
	}

	public ResultHolder<Product> detailByWebClient(String rId, String pId) {

		return webClientProductService.detail(rId, pId);
	}

}
