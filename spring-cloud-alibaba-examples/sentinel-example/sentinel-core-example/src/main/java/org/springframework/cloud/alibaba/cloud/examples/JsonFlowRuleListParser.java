package org.springframework.cloud.alibaba.cloud.examples;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * @author fangjian
 */
public class JsonFlowRuleListParser implements ConfigParser<String, List<FlowRule>> {
	@Override
	public List<FlowRule> parse(String source) {
		return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
	}
}
