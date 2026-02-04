/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.seata.autoconfigure;

import javax.sql.DataSource;

import org.apache.seata.common.holder.ObjectHolder;
import org.apache.seata.rm.fence.SpringFenceConfig;
import org.apache.seata.spring.boot.autoconfigure.SeataCoreAutoConfiguration;
import org.apache.seata.spring.boot.autoconfigure.StarterConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.transaction.PlatformTransactionManager;

import static org.apache.seata.common.Constants.BEAN_NAME_SPRING_FENCE_CONFIG;

/**
 * Spring fence auto configuration for Spring Cloud Alibaba.
 *
 * <p><strong>IMPORTANT: This is a temporary workaround class.</strong></p>
 *
 * <p>This class is a replacement for the original
 * {@code org.apache.seata.spring.boot.autoconfigure.SeataSpringFenceAutoConfiguration}
 * which has compatibility issues with Spring Boot 4.x.
 * </p>
 *
 * <p>The original class uses {@code @AutoConfigureAfter} with class names that don't exist
 * in Spring Boot 4.x, causing {@code ClassNotFoundException} during bean name generation.
 * This class fixes the issue by removing the problematic class name references.
 * </p>
 *
 * <p>The original Seata class is excluded via {@link SeataSpringFenceAutoConfigurationImportFilter},
 * and this class provides the same functionality with Spring Boot 4.x compatibility.
 * </p>
 *
 * <p><strong>This class will be removed once Seata releases a new version that fixes the
 * compatibility issue with Spring Boot 4.x. At that time, the SCA Seata module will remove
 * this class and {@link SeataSpringFenceAutoConfigurationImportFilter}.</strong></p>
 *
 * @author freeman
 * @see SeataSpringFenceAutoConfigurationImportFilter
 * @deprecated This is a temporary workaround. Will be removed after Seata releases a compatible version.
 */
@Deprecated
@ConditionalOnExpression("${seata.enabled:true}")
@ConditionalOnBean(type = {"javax.sql.DataSource", "org.springframework.transaction.PlatformTransactionManager"})
@ConditionalOnMissingBean(SpringFenceConfig.class)
@AutoConfigureAfter(
        value = {SeataCoreAutoConfiguration.class},
        name = {
            // Spring Boot 2.x, 3.x
            "org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration",
            // Spring Boot 4.x
            "org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration"
        })
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class ScaSeataSpringFenceAutoConfiguration {

	public static final String SPRING_FENCE_DATA_SOURCE_BEAN_NAME = "seataSpringFenceDataSource";
	public static final String SPRING_FENCE_TRANSACTION_MANAGER_BEAN_NAME = "seataSpringFenceTransactionManager";

	@Bean
	@ConfigurationProperties(StarterConstants.TCC_FENCE_PREFIX)
	public SpringFenceConfig springFenceConfig(
			DataSource dataSource,
			PlatformTransactionManager transactionManager,
			@Qualifier(SPRING_FENCE_DATA_SOURCE_BEAN_NAME) @Autowired(required = false)
			DataSource springFenceDataSource,
			@Qualifier(SPRING_FENCE_TRANSACTION_MANAGER_BEAN_NAME) @Autowired(required = false)
			PlatformTransactionManager springFenceTransactionManager) {
		SpringFenceConfig springFenceConfig = new SpringFenceConfig(
				springFenceDataSource != null ? springFenceDataSource : dataSource,
				springFenceTransactionManager != null ? springFenceTransactionManager : transactionManager);
		ObjectHolder.INSTANCE.setObject(BEAN_NAME_SPRING_FENCE_CONFIG, springFenceConfig);
		return springFenceConfig;
	}
}
