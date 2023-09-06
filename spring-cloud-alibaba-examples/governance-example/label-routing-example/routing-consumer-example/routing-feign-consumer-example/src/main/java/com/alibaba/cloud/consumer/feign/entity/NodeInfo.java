package com.alibaba.cloud.consumer.feign.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Component
public final class NodeInfo {

	private static Map<String, List<Map<String, List<String>>>> nodeIno = new ConcurrentHashMap<>();;

	private NodeInfo() {
	}

	public static void set(String var1, List<Map<String, List<String>>> var2) {

		nodeIno.put(var1, var2);
	}

	public static List<Map<String, List<String>>> get(String var) {

		return nodeIno.get(var);
	}

	public static Map<String, List<Map<String, List<String>>>> getNodeIno() {

		return nodeIno;
	}

}
