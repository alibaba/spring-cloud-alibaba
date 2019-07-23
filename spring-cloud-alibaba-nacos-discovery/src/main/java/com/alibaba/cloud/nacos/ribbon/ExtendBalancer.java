package com.alibaba.cloud.nacos.ribbon;

import java.util.List;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;

/**
 * @author itmuch.com
 */
public class ExtendBalancer extends Balancer {
	/**
	 * 根据权重，随机选择实例
	 *
	 * @param instances 实例列表
	 * @return 选择的实例
	 */
	public static Instance getHostByRandomWeight2(List<Instance> instances) {
		return getHostByRandomWeight(instances);
	}
}
