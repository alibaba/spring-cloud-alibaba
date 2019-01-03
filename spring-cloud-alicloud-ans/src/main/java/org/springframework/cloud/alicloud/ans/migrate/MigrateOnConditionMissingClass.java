package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionMissingClass extends MigrateOnConditionClass {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = !super.matches(context, metadata);
		logMatchResult(result);
		return result;
	}

}