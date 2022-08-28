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

package io.appactive.demo.common.service.springcloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.appactive.demo.common.RPCType;
import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductDAO {

	@Autowired(required = false)
	RestTemplate restTemplate;

	@Autowired
	private ProductService productService;

	public ResultHolder<List<Product>> list() {
		return productService.list();
	}

	public ResultHolder<Product> detail(String rId, String pId) {
		return productService.detail(rId, pId);
	}

	public ResultHolder<Product> detailHidden(String pId) {
		return productService.detailHidden(pId);
	}

	public ResultHolder<String> buy(String rId, String pId, Integer number) {
		return productService.buy(RPCType.SpringCloud.name(), rId, pId, number);
	}

	public ResultHolder<List<Product>> listTemplate() {
		if (restTemplate != null) {
			return restTemplate.getForObject("http://product/list", ResultHolder.class);
		}
		return productService.list();
	}

	public ResultHolder<Product> detailTemplate(String rId, String pId) {
		if (restTemplate != null) {
			Map<String, String> params = new HashMap<>(2);
			params.put("rId", rId);
			params.put("pId", pId);
			return restTemplate.getForObject("http://product/detail", ResultHolder.class,
					params);
		}
		return productService.detail(rId, pId);
	}

	@FeignClient(name = "product")
	public interface ProductService {

		@RequestMapping("/list/")
		ResultHolder<List<Product>> list();

		@RequestMapping("/detail/")
		ResultHolder<Product> detail(@RequestParam(name = "rId") String rId,
				@RequestParam(name = "pId") String pId);

		@RequestMapping("/detailHidden/")
		ResultHolder<Product> detailHidden(@RequestParam(name = "pId") String pId);

		@RequestMapping("/buy/")
		ResultHolder<String> buy(@RequestParam(name = "rpcType") String rpcType,
				@RequestParam(name = "rId") String rId,
				@RequestParam(name = "pId") String pId,
				@RequestParam(name = "number") Integer number);

	}

}
