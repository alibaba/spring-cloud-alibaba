package org.springframework.cloud.alibaba.cloud.examples.dubbo.provider;

import org.springframework.cloud.alibaba.cloud.examples.dubbo.FooService;

import com.alibaba.dubbo.config.annotation.Service;

/**
 * @author fangjian
 */
@Service
public class FooServiceImpl implements FooService {

	@Override
	public String hello(String name) {
		return "hello, " + name;
	}
}
