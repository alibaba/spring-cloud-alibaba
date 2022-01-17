package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sentinel feign client properties.
 *
 * @author freeman
 */
@ConfigurationProperties("feign.sentinel")
public class SentinelFeignClientProperties {

	/**
	 * default rule name.
	 */
	private String defaultRule = "default";

	/**
	 * enable refresh circuit breaker rules from config center.
	 */
	private boolean refreshRules = true;

	private Map<String, List<DegradeRule>> rules = new HashMap<>();

	public String getDefaultRule() {
		return defaultRule;
	}

	public void setDefaultRule(String defaultRule) {
		this.defaultRule = defaultRule;
	}

	public boolean isRefreshRules() {
		return refreshRules;
	}

	public void setRefreshRules(boolean refreshRules) {
		this.refreshRules = refreshRules;
	}

	public Map<String, List<DegradeRule>> getRules() {
		return rules;
	}

	public void setRules(Map<String, List<DegradeRule>> rules) {
		this.rules = rules;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SentinelFeignClientProperties that = (SentinelFeignClientProperties) o;
		return refreshRules == that.refreshRules
				&& Objects.equals(defaultRule, that.defaultRule)
				&& Objects.equals(rules, that.rules);
	}

	@Override
	public int hashCode() {
		return Objects.hash(defaultRule, refreshRules, rules);
	}

	public SentinelFeignClientProperties copy() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(this);
			return objectMapper.readValue(json, this.getClass());
		} catch (JsonProcessingException ignored) {
		}
		return new SentinelFeignClientProperties();
	}

}
