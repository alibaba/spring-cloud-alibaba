package org.springframework.cloud.alicloud.ans.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Endpoint(id = "migrate")
public class MigrateEndpoint {

	private static final Log log = LogFactory.getLog(MigrateEndpoint.class);

	public MigrateEndpoint() {
	}

	/**
	 * @return ans endpoint
	 */
	@ReadOperation
	public Map<String, ConcurrentMap<String, ServerWrapper>> invoke() {

		Map<String, ConcurrentMap<String, ServerWrapper>> result = ServerListInvocationHandler
				.getServerRegistry();

		log.info("migrate server list :" + result);
		return result;
	}
}