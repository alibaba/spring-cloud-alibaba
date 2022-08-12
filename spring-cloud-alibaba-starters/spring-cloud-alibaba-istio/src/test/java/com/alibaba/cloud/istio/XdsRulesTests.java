package com.alibaba.cloud.istio;

import com.alibaba.cloud.istio.rules.manager.IpBlockRuleManager;
import com.alibaba.cloud.istio.rules.manager.JwtAuthRuleManager;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = XdsRulesTests.TestConfig.class,
        properties = {
                "spring.cloud.istio.config.host=39.105.35.234",
                "spring.cloud.istio.config.port=15010",
                "spring.cloud.istio.config.enabled=true",
                "spring.cloud.istio.config.polling-pool-size=10",
                "spring.cloud.istio.config.polling-timeout=30"
        },
        webEnvironment = NONE
)
public class XdsRulesTests {
    @Test
    public void testIpBlockRules() {
        boolean isAllow = IpBlockRuleManager.isValid("127.0.0.1", "10.2.5.4", "192.168.6.7");
        Assertions.assertTrue(isAllow);
    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({XdsAutoConfiguration.class})
    public static class TestConfig {

    }

}
