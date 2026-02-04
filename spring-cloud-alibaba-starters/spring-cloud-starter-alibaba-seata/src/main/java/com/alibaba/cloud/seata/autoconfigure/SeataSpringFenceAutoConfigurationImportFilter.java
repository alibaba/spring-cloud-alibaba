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

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

/**
 * An {@link AutoConfigurationImportFilter} that excludes the original
 * {@code org.apache.seata.spring.boot.autoconfigure.SeataSpringFenceAutoConfiguration}
 * from Seata's auto-configuration.
 *
 * <p><strong>IMPORTANT: This is a temporary workaround class.</strong></p>
 *
 * <p>The original Seata class has compatibility issues with Spring Boot 4.x due to its use of
 * {@code @AutoConfigureAfter} annotation referencing class names that don't exist
 * in Spring Boot 4.x (e.g., {@code TransactionAutoConfiguration} moved to a different package).
 * </p>
 *
 * <p>This filter works in conjunction with {@link ScaSeataSpringFenceAutoConfiguration}
 * which provides the same functionality without the compatibility issues.
 * </p>
 *
 * <p><strong>This class will be removed once Seata releases a new version that fixes the
 * compatibility issue with Spring Boot 4.x. At that time, the SCA Seata module will remove
 * this filter and {@link ScaSeataSpringFenceAutoConfiguration}.</strong></p>
 *
 * @author uuuyuqi
 * @see ScaSeataSpringFenceAutoConfiguration
 * @deprecated This is a temporary workaround. Will be removed after Seata releases a compatible version.
 */
@Deprecated
public class SeataSpringFenceAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

	/**
	 * The fully qualified class name of the original SeataSpringFenceAutoConfiguration
	 * that needs to be excluded.
	 */
	private static final String EXCLUDED_CLASS = "org.apache.seata.spring.boot.autoconfigure.SeataSpringFenceAutoConfiguration";

	@Override
	public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
		boolean[] matches = new boolean[autoConfigurationClasses.length];
		for (int i = 0; i < autoConfigurationClasses.length; i++) {
			matches[i] = !EXCLUDED_CLASS.equals(autoConfigurationClasses[i]);
		}
		return matches;
	}

}
