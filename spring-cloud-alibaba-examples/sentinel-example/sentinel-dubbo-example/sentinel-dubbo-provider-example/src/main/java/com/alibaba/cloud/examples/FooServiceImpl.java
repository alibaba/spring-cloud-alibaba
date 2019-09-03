package com.alibaba.cloud.examples;

import org.apache.dubbo.config.annotation.Service;

/**
 * @author fangjian
 */
@Service(version = "${foo.service.version}", application = "${dubbo.application.id}", protocol = "${dubbo.protocol.id}", registry = "${dubbo.registry.id}")
public class FooServiceImpl implements FooService {

	@Override
	public String hello(String name) {
		return "hello, " + name;
	}
}