package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.loadbalancer.ILoadBalancer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pbting
 */
@Component
public class MigrateRefreshEventListener implements ApplicationListener<RefreshEvent> {
	private final static Log log = LogFactory.getLog(MigrateRefreshEventListener.class);

	private final static String MIGRATE_SWITCH = "sca.migrate.ans.switch";

	private volatile String lastScaMigrateAnsSwitchValue = "true";

	private Environment environment;

	private NamedContextFactory namedContextFactory;

	public MigrateRefreshEventListener(Environment environment,
			NamedContextFactory namedContextFactory) {
		this.environment = environment;
		this.namedContextFactory = namedContextFactory;
	}

	@PostConstruct
	public void initTimerCheck() {
		Executors.newSingleThreadScheduledExecutor()
				.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						try {
							onApplicationEvent(null);
						}
						catch (Exception e) {
							log.error(
									"check the value of 'sca.migrate.ans.switch' in environment whether changed or not cause an Exeption",
									e);
						}
					}
				}, 1, 1, TimeUnit.SECONDS);
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
		Set<String> serviceIds = MigrateProxyManager.getServicesId();
		for (Iterator<String> iterator = serviceIds.iterator(); iterator.hasNext();) {
			namedContextFactory.getInstance(iterator.next(), ILoadBalancer.class);
		}
	}

	private synchronized void updateLastScaMigrateAnsResetValue(String value) {
		this.lastScaMigrateAnsSwitchValue = value;
	}
}