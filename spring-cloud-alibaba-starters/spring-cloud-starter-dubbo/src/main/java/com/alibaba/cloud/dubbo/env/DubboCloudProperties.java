/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.dubbo.env;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alibaba.cloud.dubbo.util.DubboCloudConstants.CONFIG_PROPERTY_PREFIX;
import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * Dubbo Cloud {@link ConfigurationProperties Properties}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConfigurationProperties(prefix = CONFIG_PROPERTY_PREFIX)
public class DubboCloudProperties {

	/**
	 * All services of Dubbo.
	 */
	public static final String ALL_DUBBO_SERVICES = "*";

	/**
	 * The subscribed services, the default value is "*". The multiple value will use
	 * comma(",") as the separator.
	 *
	 * @see #ALL_DUBBO_SERVICES
	 */
	private String subscribedServices = ALL_DUBBO_SERVICES;

	public String getSubscribedServices() {
		return subscribedServices;
	}

	public void setSubscribedServices(String subscribedServices) {
		this.subscribedServices = subscribedServices;
	}

	/**
	 * Get the subscribed services as a {@link Set} with configuration order.
	 * @return non-null Read-only {@link Set}
	 */
	public Set<String> subscribedServices() {

		String[] services = commaDelimitedListToStringArray(getSubscribedServices());

		if (services.length < 1) {
			return Collections.emptySet();
		}

		Set<String> subscribedServices = new LinkedHashSet<>();

		for (String service : services) {
			if (hasText(service)) { // filter blank service name
				// remove all whitespace
				subscribedServices.add(trimAllWhitespace(service));
			}
		}

		return Collections.unmodifiableSet(subscribedServices);
	}

}
