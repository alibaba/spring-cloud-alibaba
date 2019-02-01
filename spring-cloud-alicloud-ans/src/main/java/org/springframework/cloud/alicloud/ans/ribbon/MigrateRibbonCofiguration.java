package org.springframework.cloud.alicloud.ans.ribbon;

import com.netflix.client.config.IClientConfig;
import org.springframework.cloud.alicloud.ans.migrate.MigrateOnConditionClass;
import org.springframework.cloud.alicloud.ans.migrate.MigrateRibbonBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(MigrateOnConditionClass.class)
public class MigrateRibbonCofiguration {

	@Bean
	public MigrateRibbonBeanPostProcessor migrateBeanPostProcessor(IClientConfig clientConfig) {

		return new MigrateRibbonBeanPostProcessor(clientConfig);
	}
}