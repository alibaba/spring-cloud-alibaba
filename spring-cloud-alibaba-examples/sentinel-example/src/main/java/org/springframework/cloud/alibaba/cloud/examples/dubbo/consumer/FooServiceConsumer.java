package org.springframework.cloud.alibaba.cloud.examples.dubbo.consumer;

import org.springframework.cloud.alibaba.cloud.examples.dubbo.FooService;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * @author fangjian
 */
public class FooServiceConsumer {

	@Reference(url = "dubbo://127.0.0.1:25758", timeout = 3000)
	private FooService fooService;

	public String hello(String name) {
		return fooService.hello(name);
	}

}
