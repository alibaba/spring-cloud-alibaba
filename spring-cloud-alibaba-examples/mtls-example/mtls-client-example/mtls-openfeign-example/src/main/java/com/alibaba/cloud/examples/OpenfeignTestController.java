package com.alibaba.cloud.examples;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.examples.feignclient.MvcClient;
import com.alibaba.cloud.examples.feignclient.WebfluxClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenfeignTestController {

	@Resource
	MvcClient mvcClient;

	@Resource
	WebfluxClient webfluxClient;

	@GetMapping("/openfeign/getMvc")
	public String getB(HttpServletRequest httpServletRequest) {
		return mvcClient.getMvc();
	}

	@GetMapping("/openfeign/getWebflux")
	public String getC(HttpServletRequest httpServletRequest) {
		return webfluxClient.getWebflux();
	}

}