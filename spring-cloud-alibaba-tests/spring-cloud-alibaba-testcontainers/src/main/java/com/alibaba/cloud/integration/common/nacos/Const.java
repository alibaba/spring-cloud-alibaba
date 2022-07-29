package com.alibaba.cloud.integration.common.nacos;

public class Const {

	public static final Integer NACOS_SERVER_PORT = 8848;
	public static final Integer LOCAL_SERVER_PORT = 8849;
	public static final String DEFAULT_IMAGE_NAME = System.getenv()
			.getOrDefault("TEST_IMAGE_NAME", "nacos-server-test");
	private static final String CONFIG_INSTANCE_PATH = "/nacos/v1/ns";
	public static final String NACOS_SERVER_URL = "http://127.0.0.1:" + NACOS_SERVER_PORT
			+ CONFIG_INSTANCE_PATH;

}
