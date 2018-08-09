package org.springframework.cloud.alibaba.cloud.examples.dubbo;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.cloud.examples.dubbo.provider.ProviderApplication;
import org.springframework.stereotype.Component;

/**
 * @author fangjian
 */
@Component
public class DubboProviderRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		SpringApplicationBuilder providerBuilder = new SpringApplicationBuilder()
				.bannerMode(Banner.Mode.OFF).registerShutdownHook(false)
				.logStartupInfo(false).web(WebApplicationType.NONE);
		providerBuilder.sources(ProviderApplication.class).run(args);
	}

}
