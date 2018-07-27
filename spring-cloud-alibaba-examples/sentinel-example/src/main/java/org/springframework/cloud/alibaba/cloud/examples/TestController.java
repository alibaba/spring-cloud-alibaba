package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.cloud.alibaba.sentinel.custom.EnableSentinel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaojing
 */
@RestController
public class TestController {

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	@EnableSentinel("resource")
	public String hello() {
		return "Hello";
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test1() {
		return "Hello test";
	}

}
