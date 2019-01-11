package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@ConditionalOnWebApplication
@ConditionalOnClass(value = Endpoint.class)
@Conditional(MigrateOnConditionClass.class)
public class MigrateEndpointAutoConfiguration {

	@Bean
	public MigrateEndpoint ansEndpoint() {
		return new MigrateEndpoint();
	}
}