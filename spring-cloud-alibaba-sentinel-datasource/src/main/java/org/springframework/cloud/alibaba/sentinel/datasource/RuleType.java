package org.springframework.cloud.alibaba.sentinel.datasource;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.cloud.alibaba.sentinel.datasource.config.AbstractDataSourceProperties;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * Enum for {@link AbstractRule} class, using in
 * {@link AbstractDataSourceProperties#ruleType}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public enum RuleType {

	/**
	 * flow
	 */
	FLOW("flow", FlowRule.class),
	/**
	 * degrade
	 */
	DEGRADE("degrade", DegradeRule.class),
	/**
	 * param flow
	 */
	PARAM_FLOW("param-flow", ParamFlowRule.class),
	/**
	 * system
	 */
	SYSTEM("system", SystemRule.class),
	/**
	 * authority
	 */
	AUTHORITY("authority", AuthorityRule.class);

	/**
	 * alias for {@link AbstractRule}
	 */
	private final String name;

	/**
	 * concrete {@link AbstractRule} class
	 */
	private final Class clazz;

	RuleType(String name, Class clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public Class getClazz() {
		return clazz;
	}

	public static Optional<RuleType> getByName(String name) {
		if (StringUtils.isEmpty(name)) {
			return Optional.empty();
		}
		return Arrays.stream(RuleType.values())
				.filter(ruleType -> name.equals(ruleType.getName())).findFirst();
	}

	public static Optional<RuleType> getByClass(Class clazz) {
		return Arrays.stream(RuleType.values())
				.filter(ruleType -> clazz == ruleType.getClazz()).findFirst();
	}

}
