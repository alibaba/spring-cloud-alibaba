package com.alibaba.cloud.example.common;

import java.util.List;

import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yuluo
 */

public interface WebRequest {

	ResultHolder<List<Product>> list();

	ResultHolder<Product> detail(
			@RequestParam(name = "rId") String rId,
			@RequestParam(name = "pId") String pId
	);

	ResultHolder<Product> detailHidden(
			@RequestParam(name = "pId") String pId
	);

	ResultHolder<String> buy(
			@RequestParam(name = "rpcType") String rpcType,
			@RequestParam(name = "rId") String rId,
			@RequestParam(name = "pId") String pId,
			@RequestParam(name = "number") Integer number
	);

}
