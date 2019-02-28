package org.springframework.cloud.alicloud.ans.migrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author pbting
 */
public class MigrateEndpoint
		extends AbstractEndpoint<Map<String, ConcurrentMap<String, ServerWrapper>>> {

	private static final Logger log = LoggerFactory.getLogger(MigrateEndpoint.class);

	public MigrateEndpoint() {
		super("migrate");
	}

	/**
	 * @return ans endpoint
	 */
	@Override
	public Map<String, ConcurrentMap<String, ServerWrapper>> invoke() {

		Map<String, ConcurrentMap<String, ServerWrapper>> result = ServerListInvocationHandler
				.getServerRegistry();

		log.info("migrate server list :" + result);
		return result;
	}
}