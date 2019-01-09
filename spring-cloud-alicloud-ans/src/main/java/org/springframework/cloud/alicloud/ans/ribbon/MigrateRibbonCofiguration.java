package org.springframework.cloud.alicloud.ans.ribbon;

import org.springframework.cloud.alicloud.ans.migrate.MigrateRibbonBeanPostProcessor;
import org.springframework.cloud.alicloud.ans.migrate.MigrateOnConditionClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;

@Configuration
@Conditional(MigrateOnConditionClass.class)
public class MigrateRibbonCofiguration {

	@Bean
	public MigrateRibbonBeanPostProcessor migrateBeanPostProcessor(IClientConfig clientConfig) {

		return new MigrateRibbonBeanPostProcessor(clientConfig);
	}
}