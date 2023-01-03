package com.alibaba.cloud.nacos.util;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.commons.util.InetUtilsProperties;

public class InetIPv6UtilTest {
	private final InetIPv6Util inetIPv6Util = new InetIPv6Util(new InetUtilsProperties());

	@Test
	public void getIPv6(){
		System.out.println(inetIPv6Util.findIPv6Address());
	}
}
