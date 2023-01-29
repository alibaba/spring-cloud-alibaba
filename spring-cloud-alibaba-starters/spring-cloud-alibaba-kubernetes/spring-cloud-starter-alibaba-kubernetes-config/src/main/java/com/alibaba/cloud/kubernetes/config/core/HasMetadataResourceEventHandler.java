/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.kubernetes.config.core;

import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;
import com.alibaba.cloud.kubernetes.config.util.Converters;
import com.alibaba.cloud.kubernetes.config.util.RefreshContext;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Freeman
 */
public class HasMetadataResourceEventHandler<T extends HasMetadata>
		implements ResourceEventHandler<T> {
	private static final Logger log = LoggerFactory
			.getLogger(HasMetadataResourceEventHandler.class);

	private final ApplicationContext context;
	private final ConfigurableEnvironment environment;
	private final KubernetesConfigProperties properties;

	public HasMetadataResourceEventHandler(ApplicationContext context,
			ConfigurableEnvironment environment, KubernetesConfigProperties properties) {
		this.context = context;
		this.environment = environment;
		this.properties = properties;
	}

	@Override
	public void onAdd(HasMetadata obj) {
		if (log.isDebugEnabled()) {
			log.debug("{} '{}' added in namespace '{}'", obj.getKind(),
					obj.getMetadata().getName(), obj.getMetadata().getNamespace());
		}
		// When application start up, the informer will trigger an onAdd event, but at
		// this phase application is not
		// ready, and it will not trigger a real refresh.
		// see
		// org.springframework.cloud.endpoint.event.RefreshEventListener#handle(RefreshEvent)
		refresh(obj);
	}

	@Override
	public void onUpdate(HasMetadata oldObj, HasMetadata newObj) {
		if (log.isDebugEnabled()) {
			log.debug("{} '{}' updated in namespace '{}'", newObj.getKind(),
					newObj.getMetadata().getName(), newObj.getMetadata().getNamespace());
		}
		refresh(newObj);
	}

	@Override
	public void onDelete(HasMetadata obj, boolean deletedFinalStateUnknown) {
		if (log.isDebugEnabled()) {
			log.debug("{} '{}' deleted in namespace '{}'", obj.getKind(),
					obj.getMetadata().getName(), obj.getMetadata().getNamespace());
		}
		if (properties.isRefreshOnDelete()) {
			deletePropertySourceOfResource(obj);
			refresh(obj);
		}
		else {
			log.info("Refresh on delete is disabled, ignore the delete event");
		}
	}

	private void deletePropertySourceOfResource(HasMetadata resource) {
		String propertySourceName = Converters.propertySourceNameForResource(resource);
		environment.getPropertySources().remove(propertySourceName);
	}

	private void refresh(HasMetadata obj) {
		// Need to handle the case where events are processed asynchronously?
		RefreshEvent refreshEvent = new RefreshEvent(obj, null,
				String.format("%s changed", obj.getKind()));
		RefreshContext.set(new RefreshContext(context, refreshEvent));
		try {
			context.publishEvent(refreshEvent);
		}
		finally {
			RefreshContext.remove();
		}
	}
}
