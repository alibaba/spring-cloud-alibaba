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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Convert sentinel rules for xml array Using strict mode to parse xml
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see FlowRule
 * @see DegradeRule
 * @see SystemRule
 * @see AuthorityRule
 * @see ParamFlowRule
 */
public class XmlConverter implements Converter<String, List<AbstractRule>> {

	private static final Logger logger = LoggerFactory.getLogger(XmlConverter.class);

	private final XmlMapper xmlMapper;

	public XmlConverter(XmlMapper xmlMapper) {
		this.xmlMapper = xmlMapper;
	}

	@Override
	public List<AbstractRule> convert(String source) {
		List<AbstractRule> ruleList = new ArrayList<>();
		if (StringUtils.isEmpty(source)) {
			logger.warn(
					"Sentinel XmlConverter can not convert rules because source is empty");
			return ruleList;
		}
		try {
			List xmlArray = xmlMapper.readValue(source,
					new TypeReference<List<HashMap>>() {
					});
			xmlArray.stream().forEach(obj -> {

				String itemXml = null;
				try {
					itemXml = xmlMapper.writeValueAsString(obj);
				}
				catch (JsonProcessingException e) {
					// won't be happen
				}

				List<AbstractRule> rules = Arrays.asList(convertFlowRule(itemXml),
						convertDegradeRule(itemXml), convertSystemRule(itemXml),
						convertAuthorityRule(itemXml), convertParamFlowRule(itemXml));

				List<AbstractRule> convertRuleList = rules.stream()
						.filter(rule -> !ObjectUtils.isEmpty(rule))
						.collect(Collectors.toList());

				if (convertRuleList.size() == 0) {
					logger.warn(
							"Sentinel XmlConverter can not convert {} to any rules, ignore",
							itemXml);
				}
				else if (convertRuleList.size() > 1) {
					logger.warn(
							"Sentinel XmlConverter convert {} and match multi rules, ignore",
							itemXml);
				}
				else {
					ruleList.add(convertRuleList.get(0));
				}

			});
			if (xmlArray.size() != ruleList.size()) {
				logger.warn(
						"Sentinel XmlConverter Source list size is not equals to Target List, maybe a "
								+ "part of xml is invalid. Source List: " + xmlArray
								+ ", Target List: " + ruleList);
			}
		}
		catch (Exception e) {
			logger.error("Sentinel XmlConverter convert error: " + e.getMessage());
			throw new RuntimeException(
					"Sentinel XmlConverter convert error: " + e.getMessage(), e);
		}
		return ruleList;
	}

	private FlowRule convertFlowRule(String xml) {
		try {
			FlowRule rule = xmlMapper.readValue(xml, FlowRule.class);
			if (FlowRuleManager.isValidRule(rule)) {
				return rule;
			}
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

	private DegradeRule convertDegradeRule(String xml) {
		try {
			DegradeRule rule = xmlMapper.readValue(xml, DegradeRule.class);
			if (DegradeRuleManager.isValidRule(rule)) {
				return rule;
			}
		}
		catch (Exception e) {
			// ignore
		}
		return null;
	}

	private SystemRule convertSystemRule(String xml) {
		SystemRule rule = null;
		try {
			rule = xmlMapper.readValue(xml, SystemRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

	private AuthorityRule convertAuthorityRule(String xml) {
		AuthorityRule rule = null;
		try {
			rule = xmlMapper.readValue(xml, AuthorityRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

	private ParamFlowRule convertParamFlowRule(String json) {
		ParamFlowRule rule = null;
		try {
			rule = xmlMapper.readValue(json, ParamFlowRule.class);
		}
		catch (Exception e) {
			// ignore
		}
		return rule;
	}

}
