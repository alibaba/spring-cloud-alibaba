package org.springframework.cloud.alibaba.sentinel.datasource.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Convert sentinel rules for json or xml array Using strict mode to parse json or xml
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see FlowRule
 * @see DegradeRule
 * @see SystemRule
 * @see AuthorityRule
 * @see ParamFlowRule
 * @see ObjectMapper
 */
public abstract class SentinelConverter<T extends AbstractRule>
		implements Converter<String, List<AbstractRule>> {

	private static final Logger logger = LoggerFactory.getLogger(SentinelConverter.class);

	private final ObjectMapper objectMapper;

	private final Class<T> ruleClass;

	public SentinelConverter(ObjectMapper objectMapper, Class<T> ruleClass) {
		this.objectMapper = objectMapper;
		this.ruleClass = ruleClass;
	}

	@Override
	public List<AbstractRule> convert(String source) {
		List<AbstractRule> ruleList = new ArrayList<>();
		if (StringUtils.isEmpty(source)) {
			logger.warn("converter can not convert rules because source is empty");
			return ruleList;
		}
		try {
			List sourceArray = objectMapper.readValue(source,
					new TypeReference<List<HashMap>>() {
					});
			sourceArray.stream().forEach(obj -> {

				String item = null;
				try {
					item = objectMapper.writeValueAsString(obj);
				}
				catch (JsonProcessingException e) {
					// won't be happen
				}

				Optional.ofNullable(convertRule(item))
						.ifPresent(convertRule -> ruleList.add(convertRule));
			});

			if (ruleList.size() != sourceArray.size()) {
				throw new IllegalArgumentException("convert " + ruleList.size()
						+ " rules but there are " + sourceArray.size()
						+ " rules from datasource. RuleClass: "
						+ ruleClass.getSimpleName());
			}
		}
		catch (Exception e) {
			throw new RuntimeException("convert error: " + e.getMessage(), e);
		}
		return ruleList;
	}

	private AbstractRule convertRule(String ruleStr) {
		try {
			final AbstractRule rule = objectMapper.readValue(ruleStr, ruleClass);
			RuleType ruleType = RuleType.getByClass(ruleClass).get();
			switch (ruleType) {
			case FLOW:
				if (!FlowRuleUtil.isValidRule((FlowRule) rule)) {
					return null;
				}
				break;
			case DEGRADE:
				if (!DegradeRuleManager.isValidRule((DegradeRule) rule)) {
					return null;
				}
			default:
				break;
			}
			return rule;
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

}
