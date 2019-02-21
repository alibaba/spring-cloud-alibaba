package org.springframework.cloud.alicloud.ans.migrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionClass extends MigrateOnCondition {

	private static final Logger log = LoggerFactory
			.getLogger(MigrateOnConditionClass.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = isPresent(conditionOnClass[0], classLoader)
				|| isPresent(conditionOnClass[1], classLoader);
		log.info("the result of matcher is " + result);
		return result;
	}
}