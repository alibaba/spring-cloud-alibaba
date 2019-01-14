package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.loadbalancer.ILoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pbting
 */
@Component
public class MigrateRefreshEventListener implements ApplicationListener<RefreshEvent> {

	private final static String MIGRATE_SWITCH = "sca.migrate.ans.switch";

	private volatile String lastScaMigrateAnsSwitchValue = "true";

	@Autowired
	private Environment environment;

	@Autowired
	private NamedContextFactory namedContextFactory;

	public MigrateRefreshEventListener() {
	}

	@PostConstruct
	public void initTimerCheck() {
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
				() -> onApplicationEvent(null), 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void onApplicationEvent(RefreshEvent event) {
		String value = environment.getProperty(MIGRATE_SWITCH, "true");
		// check 1: check the value
		if (value.equals(lastScaMigrateAnsSwitchValue)) {
			return;
		}

		updateLastScaMigrateAnsResetValue(value);

		// step 1: migrate up
		if ("true".equals(value)) {
			MigrateProxyManager.migrateProxyUp();
			serviceIdContextInit();
			return;
		}

		// step 2: migrate close
		if ("false".equals(value)) {
			MigrateProxyManager.migrateProxyClose();
			serviceIdContextInit();
			return;
		}
	}

	private void serviceIdContextInit() {
		namedContextFactory.destroy();
		// initializer each spring context for service id
		MigrateProxyManager.getServicesId().forEach(serviceId -> namedContextFactory
				.getInstance(serviceId, ILoadBalancer.class));
	}

	private synchronized void updateLastScaMigrateAnsResetValue(String value) {
		this.lastScaMigrateAnsSwitchValue = value;
	}
}