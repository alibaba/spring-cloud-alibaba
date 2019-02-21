package org.springframework.cloud.alicloud.ans.migrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionMissingClass extends MigrateOnConditionClass {
	private static final Logger log = LoggerFactory
			.getLogger(MigrateOnConditionMissingClass.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = !super.matches(context, metadata);
		log.info(" the result of matcher is " + result);
		return result;
	}

}