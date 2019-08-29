package com.alibaba.alicloud.oss.resource;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author lich
 * @date 2019/8/29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OssBaseTest.TestConfig.class, properties = {
    "spring.application.name=oss-example",
    "server.port=18084",
    "spring.cloud.alicloud.access-key=AK",
    "spring.cloud.alicloud.secret-key=SK",
    "spring.cloud.alicloud.oss.endpoint=localhost",
    "management.endpoints.web.exposure.include=*"}, webEnvironment = NONE)
public class OssBaseTest {

    @Configuration
    @EnableAutoConfiguration
    public static class TestConfig {
    }
}
