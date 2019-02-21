package org.springframework.cloud.alicloud.ans.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionMissingClass extends MigrateOnConditionClass {

	protected static final Log log = LogFactory
			.getLog(MigrateOnConditionMissingClass.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = !super.matches(context, metadata);
		log.info("the result of match is :" + result);
		return result;
	}

}