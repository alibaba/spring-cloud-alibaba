package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * @author fangjian
 */
public class FooServiceConsumer {

	@Reference(version = "${foo.service.version}", application = "${dubbo.application.id}",
            url = "dubbo://localhost:12345", timeout = 30000)
	private FooService fooService;

	public String hello(String name) {
		return fooService.hello(name);
	}

}
