package com.alibaba.cloud.nacos.configdata;

import java.util.Collections;
import java.util.List;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * NacosConfigDataLocationResolver Tester.
 * 
 * @author freeman
 */
public class NacosConfigDataLocationResolverTest {

	private NacosConfigDataLocationResolver resolver;

	private ConfigDataLocationResolverContext context = mock(ConfigDataLocationResolverContext.class);

	private MockEnvironment environment;

	private Binder environmentBinder;

	@BeforeEach
	void setup() {
		this.environment = new MockEnvironment();
		this.environmentBinder = Binder.get(this.environment);
		this.resolver = new NacosConfigDataLocationResolver(new DeferredLog());
		when(context.getBinder()).thenReturn(environmentBinder);
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
		this.environment.setProperty(NacosConfigProperties.PREFIX + ".enabled", "false");
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
	void whenNotSetProfile_thenDataIdIsShorter() {
		environment.setProperty("spring.application.name", "nacos-test");
		String locationUri = "nacos:localhost:8888";
		NacosConfigDataResource resource = testUri(locationUri);
		assertThat(resource.getConfig().getDataId()).isEqualTo("nacos-test.properties");
	}

	@Test
	void whenCustomizeSuffix_thenOverrideDefault() {
		environment.setProperty("spring.application.name", "nacos-test");
		environment.setProperty("spring.cloud.nacos.config.file-extension", "yml");
		String locationUri = "nacos:localhost:8888";
		NacosConfigDataResource resource = testUri(locationUri);
		assertThat(resource.getConfig().getDataId()).isEqualTo("nacos-test.yml");
	}

	@Test
	void testUrisInLocationShouldOverridesProperty() {
		environment.setProperty("spring.application.name", "nacos-test");
		environment.setProperty("spring.profiles.active", "dev");
		String locationUri = "nacos:localhost:8888/group01";
		NacosConfigDataResource resource = testUri(locationUri);
		assertThat(resource.getConfig().getGroup()).isEqualTo("group01");
		assertThat(resource.getConfig().getSuffix()).isEqualTo("properties");
		assertThat(resource.getConfig().getNamespace()).isEqualTo("");
		assertThat(resource.getConfig().isRefreshEnabled()).isTrue();
		assertThat(resource.getConfig().getDataId()).isEqualTo("nacos-test-dev.properties");

		locationUri = "nacos:localhost:8888/group01/test.yml?refreshEnabled=false";
		resource = testUri(locationUri);
		assertThat(resource.getConfig().getGroup()).isEqualTo("group01");
		assertThat(resource.getConfig().getDataId()).isEqualTo("test.yml");
		assertThat(resource.getConfig().getSuffix()).isEqualTo("yml");
		assertThat(resource.getConfig().isRefreshEnabled()).isFalse();
	}

	private NacosConfigDataResource testUri(String locationUri) {
		when(context.getBootstrapContext())
				.thenReturn(mock(ConfigurableBootstrapContext.class));
		Profiles profiles = mock(Profiles.class);
		List<NacosConfigDataResource> resources = this.resolver.resolveProfileSpecific(
				context, ConfigDataLocation.of("nacos:" + locationUri), profiles);
		assertThat(resources).hasSize(1);
		return resources.get(0);
	}

	@Test
	void whenNoneInBootstrapContext_thenCreateNewConfigClientProperties() {
		ConfigurableBootstrapContext bootstrapContext = mock(
				ConfigurableBootstrapContext.class);
		when(context.getBootstrapContext()).thenReturn(bootstrapContext);
		when(bootstrapContext.isRegistered(eq(NacosConfigProperties.class)))
				.thenReturn(false);
		when(bootstrapContext.get(eq(NacosConfigProperties.class)))
				.thenReturn(new NacosConfigProperties());
		List<NacosConfigDataResource> resources = this.resolver.resolveProfileSpecific(
				context, ConfigDataLocation.of("nacos:localhost:8888/group/test.yml"),
				mock(Profiles.class));
		assertThat(resources).hasSize(1);
		verify(bootstrapContext, times(0)).get(eq(NacosConfigProperties.class));
		NacosConfigDataResource resource = resources.get(0);
		assertThat(resource.getConfig().getGroup()).isEqualTo("group");
		assertThat(resource.getConfig().getDataId()).isEqualTo("test.yml");
	}

	private NacosConfigDataResource testResolveProfileSpecific() {
		return testResolveProfileSpecific("default");
	}

	private NacosConfigDataResource testResolveProfileSpecific(String activeProfile) {
		when(context.getBootstrapContext())
				.thenReturn(mock(ConfigurableBootstrapContext.class));
		Profiles profiles = mock(Profiles.class);
		if (activeProfile != null) {
			when(profiles.getAccepted())
					.thenReturn(Collections.singletonList(activeProfile));
		}

		List<NacosConfigDataResource> resources = this.resolver.resolveProfileSpecific(
				context, ConfigDataLocation.of("nacos:localhost:8848"), profiles);
		assertThat(resources).hasSize(1);
		return resources.get(0);
	}

}
