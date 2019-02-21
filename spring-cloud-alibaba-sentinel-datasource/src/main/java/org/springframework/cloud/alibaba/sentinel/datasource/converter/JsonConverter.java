package org.springframework.cloud.alibaba.sentinel.datasource.converter;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

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
public class JsonConverter<T> extends SentinelConverter {

	public JsonConverter(ObjectMapper objectMapper, Class<T> ruleClass) {
		super(objectMapper, ruleClass);
	}

}
