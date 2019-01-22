package org.springframework.cloud.alicloud.ans.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author pbting
 */
public class MigrateOnConditionClass extends MigrateOnCondition {

	protected static final Log log = LogFactory.getLog(MigrateOnConditionClass.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean result = isPresent(conditionOnClass[0], classLoader)
				|| isPresent(conditionOnClass[1], classLoader);
		log.info("the result of match is :" + result);
		return result;
	}
}