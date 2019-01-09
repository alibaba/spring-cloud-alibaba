package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.ans.ConditionalOnAnsEnabled;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

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
}