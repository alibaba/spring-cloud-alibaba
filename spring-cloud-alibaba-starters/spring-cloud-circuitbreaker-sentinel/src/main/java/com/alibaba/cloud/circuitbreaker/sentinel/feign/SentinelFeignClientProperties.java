package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sentinel feign client properties.
 *
 * @author freeman
 */
@ConfigurationProperties("feign.sentinel")
public class SentinelFeignClientProperties {

	/**
	 * default rule name
	 */
	private String defaultRule = "default";

	private Map<String, List<DegradeRule>> rules = new HashMap<>();

	public String getDefaultRule() {
		return defaultRule;
	}

	public void setDefaultRule(String defaultRule) {
		this.defaultRule = defaultRule;
	}

	public Map<String, List<DegradeRule>> getRules() {
		return rules;
	}

	public void setRules(Map<String, List<DegradeRule>> rules) {
		this.rules = rules;
	}

}
