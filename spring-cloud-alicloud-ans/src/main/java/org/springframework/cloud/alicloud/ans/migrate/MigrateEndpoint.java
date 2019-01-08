package org.springframework.cloud.alicloud.ans.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author pbting
 */
public class MigrateEndpoint
		extends AbstractEndpoint<Map<String, ConcurrentMap<String, ServerWraper>>> {

	private static final Log log = LogFactory.getLog(MigrateEndpoint.class);

	public MigrateEndpoint() {
		super("migrate");
	}

	/**
	 * @return ans endpoint
	 */
	@Override
	public Map<String, ConcurrentMap<String, ServerWraper>> invoke() {

		Map<String, ConcurrentMap<String, ServerWraper>> result = ServerListInvocationHandler
				.getServerRegistry();

		log.info("migrate server list :" + result);
		return result;
	}
}