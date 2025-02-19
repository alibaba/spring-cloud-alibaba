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

package com.alibaba.cloud.nacos.configdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertiesPrefixer;
import com.alibaba.nacos.api.config.ConfigService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosConfigDataLocationResolver Tester.
 *
 * @author freeman
 */
public class NacosConfigDataLocationResolverTest {

	private NacosConfigDataLocationResolver resolver;

	private ConfigDataLocationResolverContext context = Mockito.mock(
			ConfigDataLocationResolverContext.class);

	private MockEnvironment environment;

	private Binder environmentBinder;

	private ConfigurableBootstrapContext bootstrapContext = Mockito.mock(
			ConfigurableBootstrapContext.class);

	@BeforeEach
	void setup() {
		this.environment = new MockEnvironment();
		this.environmentBinder = Binder.get(this.environment);
		this.resolver = new NacosConfigDataLocationResolver(new DeferredLogs());
		Mockito.when(bootstrapContext.isRegistered(ArgumentMatchers.eq(ConfigService.class))).thenReturn(true);
		Mockito.when(context.getBinder()).thenReturn(environmentBinder);
		Mockito.when(context.getBootstrapContext()).thenReturn(bootstrapContext);
	}

	@Test
	void testIsResolvable_givenIncorrectPrefix_thenReturnFalse() {
		assertThat(
				this.resolver.isResolvable(this.context, ConfigDataLocation.of("test:")))
						.isFalse();
	}

	@Test
	void testIsResolvable_givenCorrectPrefix_thenReturnTure() {
		assertThat(
				this.resolver.isResolvable(this.context, ConfigDataLocation.of("nacos:")))
						.isTrue();
		assertThat(this.resolver.isResolvable(this.context,
				ConfigDataLocation.of("optional:nacos:"))).isTrue();
	}

	@Test
	void testIsResolvable_givenDisable_thenReturnFalse() {

		String prefix = NacosPropertiesPrefixer.getPrefix(environment);

		this.environment.setProperty(prefix + ".config.enabled", "false");
		assertThat(
				this.resolver.isResolvable(this.context, ConfigDataLocation.of("nacos:")))
						.isFalse();
	}

	@Test
	void testResolveProfileSpecific_givenNothing_thenReturnDefaultProfile() {
		NacosConfigDataResource resource = testResolveProfileSpecific();
		assertThat(resource.getProfiles()).isEqualTo("default");
	}

	@Test
	void testStartWithASlashIsOK() {
		String locationUri = "nacos:/app";
		List<NacosConfigDataResource> resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getConfig().getDataId()).isEqualTo("app");

		locationUri = "nacos:app";
		resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getConfig().getDataId()).isEqualTo("app");
	}

	@Test
	void testDataIdMustBeSpecified() {
		String locationUri = "nacos:";
		Assertions.assertThatThrownBy(() -> testUri(locationUri))
				.hasMessage("dataId must be specified");
	}

	@Test
	void testInvalidDataId() {
		String locationUri = "nacos:test/test.yml";
		Assertions.assertThatThrownBy(() -> testUri(locationUri)).hasMessage("illegal dataId");
	}

	@Test
	void whenCustomizeSuffix_thenOverrideDefault() {
		String locationUri = "nacos:app";
		List<NacosConfigDataResource> resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getConfig().getDataId()).isEqualTo("app");
		assertThat(resources.get(0).getConfig().getSuffix()).isEqualTo("properties");

		environment.setProperty("spring.nacos.config.file-extension", "yml");
		locationUri = "nacos:app";
		resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getConfig().getDataId()).isEqualTo("app");
		assertThat(resources.get(0).getConfig().getSuffix()).isEqualTo("yml");

		locationUri = "nacos:app.json";
		resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getConfig().getDataId()).isEqualTo("app.json");
		assertThat(resources.get(0).getConfig().getSuffix()).isEqualTo("json");
	}

	@Test
	void testUrisInLocationShouldOverridesProperty() {
		environment.setProperty("spring.nacos.config.group", "default");
		environment.setProperty("spring.nacos.config.refreshEnabled", "true");
		String locationUri = "nacos:test.yml?group=not_default&refreshEnabled=false";
		List<NacosConfigDataResource> resources = testUri(locationUri);
		Assertions.assertThat(resources).hasSize(1);
		NacosConfigDataResource resource = resources.get(0);
		assertThat(resource.getConfig().getGroup()).isEqualTo("not_default");
		assertThat(resource.getConfig().getSuffix()).isEqualTo("yml");
		assertThat(resource.getConfig().isRefreshEnabled()).isFalse();
		assertThat(resource.getConfig().getDataId()).isEqualTo("test.yml");
	}

	@Test
	void testSetCommonPropertiesIsOK() {
		environment.setProperty("spring.nacos.username", "root");
		environment.setProperty("spring.nacos.password", "root");
		environment.setProperty("spring.nacos.server-addr", "127.0.0.1:8888");
		String locationUri = "nacos:test.yml";
		List<NacosConfigDataResource> resources = testUri(locationUri);

		Assertions.assertThat(resources).hasSize(1);
		NacosConfigDataResource resource = resources.get(0);
		assertThat(resource.getProperties().getUsername()).isEqualTo("root");
		assertThat(resource.getProperties().getPassword()).isEqualTo("root");
		assertThat(resource.getProperties().getServerAddr()).isEqualTo("127.0.0.1:8888");
	}

	@Test
	void testCommonPropertiesHasLowerPriority() {
		environment.setProperty("spring.nacos.username", "root");
		environment.setProperty("spring.nacos.password", "root");
		environment.setProperty("spring.nacos.config.password", "not_root");
		environment.setProperty("spring.nacos.server-addr", "127.0.0.1:8888");
		environment.setProperty("spring.nacos.config.server-addr",
				"127.0.0.1:9999");
		String locationUri = "nacos:test.yml";
		List<NacosConfigDataResource> resources = testUri(locationUri);

		Assertions.assertThat(resources).hasSize(1);
		NacosConfigDataResource resource = resources.get(0);
		assertThat(resource.getProperties().getUsername()).isEqualTo("root");
		assertThat(resource.getProperties().getPassword()).isEqualTo("not_root");
		assertThat(resource.getProperties().getServerAddr()).isEqualTo("127.0.0.1:9999");
	}

	private List<NacosConfigDataResource> testUri(String locationUri,
			String... activeProfiles) {
		Profiles profiles = Mockito.mock(Profiles.class);
		Mockito.when(profiles.getActive()).thenReturn(Arrays.asList(activeProfiles));
		return this.resolver.resolveProfileSpecific(context,
				ConfigDataLocation.of(locationUri), profiles);
	}

	@Test
	void whenNoneInBootstrapContext_thenCreateNewConfigClientProperties() {
		Mockito.when(bootstrapContext.isRegistered(ArgumentMatchers.eq(NacosConfigProperties.class)))
				.thenReturn(false);
		Mockito.when(bootstrapContext.get(ArgumentMatchers.eq(NacosConfigProperties.class)))
				.thenReturn(new NacosConfigProperties());
		List<NacosConfigDataResource> resources = this.resolver.resolveProfileSpecific(
				context, ConfigDataLocation.of("nacos:test.yml"), Mockito.mock(Profiles.class));
		Assertions.assertThat(resources).hasSize(1);
		Mockito.verify(bootstrapContext, Mockito.times(0)).get(ArgumentMatchers.eq(NacosConfigProperties.class));
		NacosConfigDataResource resource = resources.get(0);
		assertThat(resource.getConfig().getGroup()).isEqualTo("DEFAULT_GROUP");
		assertThat(resource.getConfig().getDataId()).isEqualTo("test.yml");
	}

	private NacosConfigDataResource testResolveProfileSpecific() {
		return testResolveProfileSpecific("default");
	}

	private NacosConfigDataResource testResolveProfileSpecific(String activeProfile) {
		Profiles profiles = Mockito.mock(Profiles.class);
		if (activeProfile != null) {
			Mockito.when(profiles.getActive())
					.thenReturn(Collections.singletonList(activeProfile));
			Mockito.when(profiles.getAccepted())
					.thenReturn(Collections.singletonList(activeProfile));
		}

		List<NacosConfigDataResource> resources = this.resolver.resolveProfileSpecific(
				context, ConfigDataLocation.of("nacos:test.yml"), profiles);
		Assertions.assertThat(resources).hasSize(1);
		return resources.get(0);
	}

}
