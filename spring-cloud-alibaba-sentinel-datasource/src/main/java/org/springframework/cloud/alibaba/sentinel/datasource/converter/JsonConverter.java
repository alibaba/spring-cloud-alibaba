package org.springframework.cloud.alibaba.sentinel.datasource.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Convert sentinel rules for json array Using strict mode to parse json
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see FlowRule
 * @see DegradeRule
 * @see SystemRule
 * @see AuthorityRule
 * @see ParamFlowRule
 * @see ObjectMapper
 */
public class JsonConverter implements Converter<String, List<AbstractRule>> {

	private static final Logger logger = LoggerFactory.getLogger(JsonConverter.class);

	private final ObjectMapper objectMapper;

	public JsonConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public List<AbstractRule> convert(String source) {
		List<AbstractRule> ruleList = new ArrayList<>();
		if (StringUtils.isEmpty(source)) {
			logger.warn(
					"Sentinel JsonConverter can not convert rules because source is empty");
			return ruleList;
		}
		try {
			List jsonArray = objectMapper.readValue(source,
					new TypeReference<List<HashMap>>() {
					});
			jsonArray.stream().forEach(obj -> {

				String itemJson = null;
				try {
					itemJson = objectMapper.writeValueAsString(obj);
				}
				catch (JsonProcessingException e) {
					// won't be happen
				}

				List<AbstractRule> rules = Arrays.asList(convertFlowRule(itemJson),
						convertDegradeRule(itemJson), convertSystemRule(itemJson),
						convertAuthorityRule(itemJson), convertParamFlowRule(itemJson));

				List<AbstractRule> convertRuleList = rules.stream()
						.filter(rule -> !ObjectUtils.isEmpty(rule))
						.collect(Collectors.toList());

				if (convertRuleList.size() == 0) {
					logger.warn(
							"Sentinel JsonConverter can not convert {} to any rules, ignore",
							itemJson);
				}
				else if (convertRuleList.size() > 1) {
					logger.warn(
							"Sentinel JsonConverter convert {} and match multi rules, ignore",
							itemJson);
				}
				else {
					ruleList.add(convertRuleList.get(0));
				}

			});
			if (jsonArray.size() != ruleList.size()) {
				logger.warn(
						"Sentinel JsonConverter Source list size is not equals to Target List, maybe a "
								+ "part of json is invalid. Source List: " + jsonArray
								+ ", Target List: " + ruleList);
			}
		}
		catch (Exception e) {
			logger.error("Sentinel JsonConverter convert error: " + e.getMessage());
			throw new RuntimeException(
					"Sentinel JsonConverter convert error: " + e.getMessage(), e);
		}
		return ruleList;
	}

	private FlowRule convertFlowRule(String json) {
		try {
			FlowRule rule = objectMapper.readValue(json, FlowRule.class);
			if (FlowRuleManager.isValidRule(rule)) {
				return rule;
			}
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

	private DegradeRule convertDegradeRule(String json) {
		try {
			DegradeRule rule = objectMapper.readValue(json, DegradeRule.class);
			if (DegradeRuleManager.isValidRule(rule)) {
				return rule;
			}
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

	private SystemRule convertSystemRule(String json) {
		SystemRule rule = null;
		try {
			rule = objectMapper.readValue(json, SystemRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

	private AuthorityRule convertAuthorityRule(String json) {
		AuthorityRule rule = null;
		try {
			rule = objectMapper.readValue(json, AuthorityRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

	private ParamFlowRule convertParamFlowRule(String json) {
		ParamFlowRule rule = null;
		try {
			rule = objectMapper.readValue(json, ParamFlowRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

}
