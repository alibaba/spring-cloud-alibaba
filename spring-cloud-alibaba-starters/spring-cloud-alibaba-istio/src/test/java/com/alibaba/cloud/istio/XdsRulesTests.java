package com.alibaba.cloud.istio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = XdsRulesTests.TestConfig.class,
        properties = {
                "spring.cloud.istio.config.host=39.106.152.73",
                "spring.cloud.istio.config.port=15010",
                "spring.cloud.istio.config.enabled=true",
                "spring.cloud.istio.config.polling-pool-size=10",
                "spring.cloud.istio.config.polling-timeout=30"
        },
        webEnvironment = NONE
)
public class XdsRulesTests {
    @Test
    public void testGetServerAddr() {

    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({XdsAutoConfiguration.class})
    public static class TestConfig {

    }

}
