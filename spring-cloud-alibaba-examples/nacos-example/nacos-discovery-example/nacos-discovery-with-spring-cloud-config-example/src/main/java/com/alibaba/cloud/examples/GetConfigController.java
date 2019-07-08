package com.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetConfigController {

	@Value("${config}")
	private String config;

	@RequestMapping(value = "/config", method = RequestMethod.GET)
	public String getConfig() {
		return config;
	}

}
