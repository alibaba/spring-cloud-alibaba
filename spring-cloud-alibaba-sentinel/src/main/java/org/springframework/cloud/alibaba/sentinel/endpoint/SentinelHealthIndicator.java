package org.springframework.cloud.alibaba.sentinel.endpoint;

import com.alibaba.csp.sentinel.heartbeat.HeartbeatSenderProvider;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link HealthIndicator} for Sentinel.
 * <p>
 * Check the status of Sentinel Dashboard by sending a heartbeat message to it.
 *
 * Note: if sentinel isn't enabled or sentinel-dashboard isn't configured,
 * the health status is up and more infos are provided in detail.
 * </p>
 *
 * @author cdfive
 */
public class SentinelHealthIndicator extends AbstractHealthIndicator {

    private SentinelProperties sentinelProperties;

    public SentinelHealthIndicator(SentinelProperties sentinelProperties) {
        this.sentinelProperties = sentinelProperties;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Map<String, Object> detailMap = new HashMap<>();

        // If sentinel isn't enabled, set the status up and set the enabled to false in detail
        if (!sentinelProperties.isEnabled()) {
            detailMap.put("enabled", false);
            builder.up().withDetails(detailMap);
            return;
        }

        detailMap.put("enabled", true);

        String consoleServer = TransportConfig.getConsoleServer();
        // If dashboard isn't configured, set the status to UNKNOWN
        if (StringUtils.isEmpty(consoleServer)) {
            detailMap.put("dashboard", new Status(Status.UNKNOWN.getCode(), "dashboard isn't configured"));
            builder.up().withDetails(detailMap);
            return;
        }

        // If dashboard is configured, send a heartbeat message to it and check the result
        HeartbeatSender heartbeatSender = HeartbeatSenderProvider.getHeartbeatSender();
        boolean result = heartbeatSender.sendHeartbeat();
        if (result) {
            detailMap.put("dashboard", Status.UP);
            builder.up().withDetails(detailMap);
        } else {
            detailMap.put("dashboard", new Status(Status.DOWN.getCode(), consoleServer + " can't be connected"));
            builder.down().withDetails(detailMap);
        }
    }
}
