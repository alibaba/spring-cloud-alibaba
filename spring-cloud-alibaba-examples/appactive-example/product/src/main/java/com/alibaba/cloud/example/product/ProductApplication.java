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

package com.alibaba.cloud.example.product;

import java.util.List;

import com.alibaba.cloud.example.common.RPCType;
import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;
import com.alibaba.cloud.example.common.service.OrderDAO;
import com.alibaba.cloud.example.common.service.ProductServiceNormal;
import com.alibaba.cloud.example.common.service.ProductServiceUnit;
import com.alibaba.cloud.example.common.service.ProductServiceUnitHidden;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@ComponentScan(basePackages = { "com.alibaba.cloud.example" })
@EntityScan("com.alibaba.cloud.example.*")
@Controller
@RequestMapping("/")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.alibaba.cloud.example" })
public class ProductApplication {

	private static final Logger logger = LogUtil.getLogger();

	@Autowired
	OrderDAO orderDAO;

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private ProductServiceNormal productServiceNormal;

	@Autowired
	private ProductServiceUnit productServiceUnit;

	@Autowired
	private ProductServiceUnitHidden productServiceUnitHidden;

	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}

	@RequestMapping("/echo")
	@ResponseBody
	public String echo(
			@RequestParam(required = false, defaultValue = "jack") String user) {
		String s = String.valueOf(user);
		return String.format("%s get %s", s, productServiceNormal.list().toString());
	}

	@RequestMapping("/list")
	@ResponseBody
	public ResultHolder<List<Product>> list() {
		return productServiceNormal.list();
	}

	@RequestMapping("/detailHidden")
	@ResponseBody
	public ResultHolder<Product> detailHidden(
			@RequestParam(required = false, defaultValue = "12") String pId) {
		// unit
		logger.info("detailHidden, routerId: {}, pId: {}", AppContextClient.getRouteId(),
				pId);
		return productServiceUnitHidden.detail(pId);
	}

	@RequestMapping("/detail")
	@ResponseBody
	public ResultHolder<Product> detail(
			@RequestParam(required = false, defaultValue = "12") String rId,
			@RequestParam(required = false, defaultValue = "12") String pId) {
		// unit
		logger.info("detail, routerId: {}, pId: {}", AppContextClient.getRouteId(), pId);
		return productServiceUnit.detail(rId, pId);
	}

	@RequestMapping("/buy")
	@ResponseBody
	public ResultHolder<String> buy(
			@RequestParam(required = false, defaultValue = "Dubbo") RPCType rpcType,
			@RequestParam(required = false, defaultValue = "12") String rId,
			@RequestParam(required = false, defaultValue = "12") String pId,
			@RequestParam(required = false, defaultValue = "5") Integer number) {
		logger.info("buy, routerId: {}, rpcType: {}", AppContextClient.getRouteId(),
				rpcType);
		return orderDAO.buy(rId, pId, number);

	}

	@RequestMapping("/check")
	@ResponseBody
	public String check() {
		return "OK From " + appName;
	}

}
