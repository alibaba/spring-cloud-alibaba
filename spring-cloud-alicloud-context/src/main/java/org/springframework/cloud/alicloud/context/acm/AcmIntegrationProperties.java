/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context.acm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author xiaolongzuo
 */
public class AcmIntegrationProperties {

	private String applicationName;

	private String applicationGroup;

	private String[] activeProfiles = new String[0];

	private AcmProperties acmProperties;

	public String getApplicationConfigurationDataIdWithoutGroup() {
		return applicationName + "." + acmProperties.getFileExtension();
	}

	public List<String> getGroupConfigurationDataIds() {
		List<String> groupConfigurationDataIds = new ArrayList<>();
		if (StringUtils.isEmpty(applicationGroup)) {
			return groupConfigurationDataIds;
		}
		String[] parts = applicationGroup.split("\\.");
		for (int i = 1; i < parts.length; i++) {
			StringBuilder subGroup = new StringBuilder(parts[0]);
			for (int j = 1; j <= i; j++) {
				subGroup.append(".").append(parts[j]);
			}
			groupConfigurationDataIds
					.add(subGroup + ":application." + acmProperties.getFileExtension());
		}
		return groupConfigurationDataIds;
	}

	public List<String> getApplicationConfigurationDataIds() {
		List<String> applicationConfigurationDataIds = new ArrayList<>();
		if (!StringUtils.isEmpty(applicationGroup)) {
			applicationConfigurationDataIds.add(applicationGroup + ":" + applicationName
					+ "." + acmProperties.getFileExtension());
			for (String profile : activeProfiles) {
				applicationConfigurationDataIds
						.add(applicationGroup + ":" + applicationName + "-" + profile
								+ "." + acmProperties.getFileExtension());
			}

		}
		applicationConfigurationDataIds
				.add(applicationName + "." + acmProperties.getFileExtension());
		for (String profile : activeProfiles) {
			applicationConfigurationDataIds.add(applicationName + "-" + profile + "."
					+ acmProperties.getFileExtension());
		}
		return applicationConfigurationDataIds;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setApplicationGroup(String applicationGroup) {
		this.applicationGroup = applicationGroup;
	}

	public void setActiveProfiles(String[] activeProfiles) {
		this.activeProfiles = activeProfiles;
	}

	public void setAcmProperties(AcmProperties acmProperties) {
		this.acmProperties = acmProperties;
	}

	public AcmProperties getAcmProperties() {
		return acmProperties;
	}
}
