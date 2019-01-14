package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.ans.ConditionalOnAnsEnabled;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties
@Conditional(MigrateOnConditionClass.class)
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@ConditionalOnAnsEnabled
public class MigrationAutoconfiguration {

	@Bean
	public MigrateServiceRegistry migrationManger(AnsProperties ansProperties,
			ApplicationContext applicationContext) {

		return new MigrateServiceRegistry(ansProperties, applicationContext);
	}

	@Bean
	public MigrateRefreshEventListener migrateRefreshEventListener(
			Environment environment,
			@Qualifier(value = "springClientFactory") NamedContextFactory namedContextFactory) {

		return new MigrateRefreshEventListener(environment, namedContextFactory);
	}
}