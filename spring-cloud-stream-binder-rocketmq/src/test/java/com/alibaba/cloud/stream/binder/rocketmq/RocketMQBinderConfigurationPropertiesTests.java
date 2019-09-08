package com.alibaba.cloud.stream.binder.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.apache.rocketmq.common.MixAll;
import org.junit.Test;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RocketMQBinderConfigurationProperties} unit test
 *
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 */
public class RocketMQBinderConfigurationPropertiesTests {

    @Test
    public void testRocketMQBinderConfigurationPropertiesTest() {

        // initialize test application context
        StaticApplicationContext context = new StaticApplicationContext();
        // add internal properties
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("spring.cloud.stream.rocketmq.binder.nameServer", "127.0.0.1:6666");
        configMap.put("spring.cloud.stream.rocketmq.binder.customizedTraceTopic",
                "customized-trace-topic");
        context.getEnvironment().getPropertySources().addLast(new MapPropertySource(
                "rocketMQBinderConfigurationProperties", configMap));

        context.registerSingleton(ConfigurationBeanFactoryMetadata.BEAN_NAME,
                ConfigurationBeanFactoryMetadata.class);

        context.registerSingleton(ConfigurationPropertiesBindingPostProcessor.BEAN_NAME,
                ConfigurationPropertiesBindingPostProcessor.class);
        // register singleton bean
        context.registerSingleton("rocketMQBinderConfigurationProperties",
                RocketMQBinderConfigurationProperties.class);

        // refresh test application context
        context.refresh();

        // using Spring Boot class to ensure that reliance on the same ObjectMapper
        // configuration
        ConfigurationPropertiesReportEndpoint endpoint = new ConfigurationPropertiesReportEndpoint();
        endpoint.setApplicationContext(context);

        ConfigurationPropertiesReportEndpoint.ContextConfigurationProperties configurationProperties = endpoint
                .configurationProperties().getContexts().values().iterator().next();

        Map<String, Object> properties = configurationProperties.getBeans()
                .get("rocketMQBinderConfigurationProperties").getProperties();

        assertThat(properties.get("nameServer").equals("127.0.0.1:6666")).isTrue();
        assertThat(properties.get("enableMsgTrace") instanceof Boolean).isTrue();
        assertThat(
                properties.get("customizedTraceTopic").equals(MixAll.RMQ_SYS_TRACE_TOPIC))
                .isFalse();
        assertThat(
                properties.get("customizedTraceTopic").equals("customized-trace-topic"))
                .isTrue();
    }

}
