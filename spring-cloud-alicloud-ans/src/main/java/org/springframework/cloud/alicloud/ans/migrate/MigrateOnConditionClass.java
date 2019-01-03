package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionClass extends MigrateOnCondition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = isPresent(conditionOnClass[0], classLoader)
				|| isPresent(conditionOnClass[1], classLoader);
		logMatchResult(result);
		return result;
	}
}