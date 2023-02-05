package com.alibaba.cloud.example.common.service.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.example.common.WebRequest;
import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author yuluo
 */

@Component
public class RestTemplateRequestStrategy implements WebRequest {

	@Autowired(required = false)
	RestTemplate restTemplate;

	@Autowired
	private FeignRequestStrategy feignRequestStrategy;

	@Override
	public ResultHolder<List<Product>> list() {
		if (restTemplate != null) {
			return restTemplate.getForObject("http://product/list", ResultHolder.class);
		}
		return feignRequestStrategy.list();
	}

	@Override
	public ResultHolder<Product> detail(String rId, String pId) {

		if (restTemplate != null) {
			Map<String, String> params = new HashMap<>(2);
			params.put("rId", rId);
			params.put("pId", pId);
			return restTemplate.getForObject("http://product/detail", ResultHolder.class,
					params);
		}

		return feignRequestStrategy.detail(rId, pId);
	}

	@Override
	public ResultHolder<Product> detailHidden(String pId) {

		return feignRequestStrategy.detailHidden(pId);
	}

	@Override
	public ResultHolder<String> buy(String rpcType, String rId, String pId, Integer number) {

		return feignRequestStrategy.buy(rpcType, rId, pId, number);
	}
}
