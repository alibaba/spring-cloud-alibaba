package org.springframework.cloud.stream.binder.rocketmq.actuator;

import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ENDPOINT_ID;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Endpoint(id = ENDPOINT_ID)
public class RocketMQBinderEndpoint {

	@Autowired(required = false)
	private InstrumentationManager instrumentationManager;

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		if (instrumentationManager != null) {
			result.put("metrics",
					instrumentationManager.getMetricRegistry().getMetrics());
			result.put("runtime", instrumentationManager.getRuntime());
		}
		else {
			result.put("warning",
					"please add metrics-core dependency, we use it for metrics");
		}
		return result;
	}

}
