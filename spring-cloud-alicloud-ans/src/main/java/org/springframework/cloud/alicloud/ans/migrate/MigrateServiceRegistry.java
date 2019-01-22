package org.springframework.cloud.alicloud.ans.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.alicloud.ans.registry.AnsRegistration;
import org.springframework.cloud.alicloud.ans.registry.AnsServiceRegistry;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pbting
 */
@Component
public class MigrateServiceRegistry {

	private static final Log log = LogFactory.getLog(MigrateServiceRegistry.class);

	private AtomicBoolean running = new AtomicBoolean(false);

	private ServiceRegistry serviceRegistry;
	private AnsRegistration ansRegistration;

	public MigrateServiceRegistry(AnsProperties ansProperties,
			ApplicationContext context) {
		this.ansRegistration = new AnsRegistration(ansProperties, context);
		this.ansRegistration.init();
		this.serviceRegistry = new AnsServiceRegistry();
	}

	@EventListener(WebServerInitializedEvent.class)
	public void onApplicationEvent(WebServerInitializedEvent event) {
		int serverPort = event.getWebServer().getPort();
		this.ansRegistration.setPort(serverPort);
		log.info("[ Migrate ] change the port to " + serverPort);
		if (!this.running.get()) {
			long s = System.currentTimeMillis();
			log.info("[Migrate] start to registry server to ANS");
			this.serviceRegistry.register(this.ansRegistration);
			log.info("[migrate] end to registry server to ANS cost time with "
					+ (System.currentTimeMillis() - s) + " ms.");
			this.running.set(true);
		}
	}

}