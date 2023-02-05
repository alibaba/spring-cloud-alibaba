package com.alibaba.cloud.example.common.service.strategy;


import java.util.List;

import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yuluo
 */

@FeignClient(name = "product")
public interface FeignRequestStrategy {

	@RequestMapping("/list/")
	ResultHolder<List<Product>> list();

	@RequestMapping("/detail/")
	ResultHolder<Product> detail(
			@RequestParam(name = "rId") String rId,
			@RequestParam(name = "pId") String pId
	);

	@RequestMapping("/detailHidden/")
	ResultHolder<Product> detailHidden(@RequestParam(name = "pId") String pId);

	@RequestMapping("/buy/")
	ResultHolder<String> buy(
			@RequestParam(name = "rpcType") String rpcType,
			@RequestParam(name = "rId") String rId,
			@RequestParam(name = "pId") String pId,
			@RequestParam(name = "number") Integer number
	);

}
