package com.alibaba.cloud.examples;

import org.apache.dubbo.config.annotation.Reference;

/**
 * @author fangjian
 */
public class FooServiceConsumer {

	@Reference(version = "${foo.service.version}", application = "${dubbo.application.id}", url = "dubbo://localhost:12345?version=1.0.0", timeout = 30000)
	private FooService fooService;

	public String hello(String name) {
		return fooService.hello(name);
	}

}
