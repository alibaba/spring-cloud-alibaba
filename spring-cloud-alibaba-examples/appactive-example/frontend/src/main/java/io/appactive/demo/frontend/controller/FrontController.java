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

package io.appactive.demo.frontend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import io.appactive.demo.common.RPCType;
import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.springcloud.ProductDAO;
import io.appactive.java.api.base.AppContextClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/")
public class FrontController {

	@Resource
	private ProductDAO productDAO;

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private Environment env;

	private Map<String, String[]> metaData;

	@RequestMapping("/")
	public String index() {
		return "redirect:/listProduct";
	}

	@RequestMapping("/echo")
	@ResponseBody
	public ResultHolder<String> echo(@RequestParam(required = false,
			defaultValue = "echo content") String content) {
		return new ResultHolder<>(appName + " : " + content);
	}

	@RequestMapping("/check")
	@ResponseBody
	public String check() {
		return "OK From " + appName;
	}

	@RequestMapping("/show")
	@ResponseBody
	public String show() {
		return "routerId: " + AppContextClient.getRouteId();
	}

	@ModelAttribute("metaData")
	public Map<String, String[]> getMetaData() {
		return metaData;
	}

	@PostConstruct
	public void parseMetaData() {
		String unitList = env.getProperty("io.appactive.demo.unitlist");
		String appList = env.getProperty("io.appactive.demo.applist");
		metaData = new HashMap<>(2);
		metaData.put("unitList", unitList.split(","));
		metaData.put("appList", appList.split(","));
	}

	@RequestMapping("/meta")
	@ResponseBody
	public ResultHolder<Object> meta() {
		return new ResultHolder<>(metaData);
	}

	@GetMapping("/listProduct")
	public String listProduct(
			@CookieValue(value = "rpc_type", required = false,
					defaultValue = "Dubbo") RPCType rpcType,
			@RequestParam(required = false, defaultValue = "feign") String call,
			Model model) {
		// normal
		ResultHolder<List<Product>> resultHolder = (call.equals("feign")
				? productDAO.list() : productDAO.listTemplate());

		model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
		model.addAttribute("products", resultHolder.getResult());
		model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
		model.addAttribute("current", "listProduct");
		return "index.html";
	}

	@GetMapping("/detailProduct")
	public String detailProduct(
			@CookieValue(value = "rpc_type", required = false,
					defaultValue = "Dubbo") RPCType rpcType,
			@RequestParam(required = false, defaultValue = "12") String id,
			@RequestParam(required = false, defaultValue = "false") Boolean hidden,
			@RequestParam(required = false, defaultValue = "feign") String call,
			Model model) {
		// unit
		ResultHolder<Product> resultHolder = getProductResultHolder(rpcType, id, hidden,
				call);

		model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
		model.addAttribute("product", resultHolder.getResult());
		model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
		model.addAttribute("current", "detailProduct");
		return "detail.html";
	}

	private ResultHolder<Product> getProductResultHolder(RPCType rpcType, String id,
			Boolean hidden, String call) {
		ResultHolder<Product> resultHolder;
		resultHolder = hidden ? productDAO.detailHidden(id)
				: (call.equals("feign")
						? productDAO.detail(AppContextClient.getRouteId(), id)
						: productDAO.detailTemplate(AppContextClient.getRouteId(), id));
		return resultHolder;
	}

	@RequestMapping("/buyProduct")
	public String buyProduct(
			@CookieValue(value = "rpc_type", required = false,
					defaultValue = "Dubbo") RPCType rpcType,
			@RequestParam(required = false, defaultValue = "12") String pId,
			@RequestParam(required = false, defaultValue = "1") Integer number,
			@RequestParam(required = false, defaultValue = "feign") String call,
			Model model) {
		// unit
		ResultHolder<String> resultHolder = productDAO.buy(AppContextClient.getRouteId(),
				pId, number);

		ResultHolder<Product> productHolder = getProductResultHolder(rpcType, pId, false,
				call);

		model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
		model.addAttribute("msg", resultHolder.getResult());
		model.addAttribute("product", productHolder.getResult());
		model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
		model.addAttribute("current", "buyProduct");
		return "buy.html";
	}

}
