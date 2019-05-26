package org.springframework.cloud.alibaba.sentinel.endpoint;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.heartbeat.HeartbeatSenderProvider;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link SentinelHealthIndicator}.
 *
 * @author cdfive
 */
public class SentinelHealthIndicatorTests {

    private SentinelHealthIndicator sentinelHealthIndicator;

    private SentinelProperties sentinelProperties;

    private HeartbeatSender heartbeatSender;

    @Before
    public void setUp() {
        sentinelProperties = mock(SentinelProperties.class);
        sentinelHealthIndicator = new SentinelHealthIndicator(sentinelProperties);

        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "");

        heartbeatSender = mock(HeartbeatSender.class);
        Field heartbeatSenderField = ReflectionUtils.findField(HeartbeatSenderProvider.class, "heartbeatSender");
        heartbeatSenderField.setAccessible(true);
        ReflectionUtils.setField(heartbeatSenderField, null, heartbeatSender);
    }

    @Test
    public void testSentinelNotEnabled() {
        when(sentinelProperties.isEnabled()).thenReturn(false);

        Health health = sentinelHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("enabled")).isEqualTo(false);
    }

    @Test
    public void testSentinelDashboardNotConfigured() {
        when(sentinelProperties.isEnabled()).thenReturn(true);

        Health health = sentinelHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("dashboard")).isEqualTo(Status.UNKNOWN);
    }

    @Test
    public void testSentinelDashboardConfiguredCheckSuccess() throws Exception {
        when(sentinelProperties.isEnabled()).thenReturn(true);
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
        when(heartbeatSender.sendHeartbeat()).thenReturn(true);


        Health health = sentinelHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void testSentinelDashboardConfiguredCheckFailed() throws Exception {
        when(sentinelProperties.isEnabled()).thenReturn(true);
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
        when(heartbeatSender.sendHeartbeat()).thenReturn(false);


        Health health = sentinelHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("dashboard")).isEqualTo(new Status(Status.DOWN.getCode(), "localhost:8080 can't be connected"));
    }
}
