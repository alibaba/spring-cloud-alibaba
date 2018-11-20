package org.springframework.cloud.stream.binder.rocketmq.actuator;

import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ENDPOINT_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Endpoint(id = ENDPOINT_ID)
public class RocketMQBinderEndpoint {

	private MetricRegistry metricRegistry = new MetricRegistry();
	private Map<String, Object> runtime = new ConcurrentHashMap<>();

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		result.put("metrics", metricRegistry().getMetrics());
		result.put("runtime", runtime());
		return result;
	}

	public MetricRegistry metricRegistry() {
		return metricRegistry;
	}

	public Map<String, Object> runtime() {
		return runtime;
	}

}
