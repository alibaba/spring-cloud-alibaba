package com.alibaba.alicloud.oss.resource;

import com.aliyun.oss.OSS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author lich
 * @date 2019/8/29
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class OssStorageResourceTest {

    @Value("oss://aliyun-test-bucket/myfilekey")
    private Resource fileResource;

    @Test
    public void testResourceType() {
        assertEquals(OssStorageResource.class, fileResource.getClass());
        OssStorageResource ossStorageResource = (OssStorageResource)fileResource;
        assertEquals("myfilekey", ossStorageResource.getFilename());
        assertFalse(ossStorageResource.isBucket());
    }

    /**
     * Configuration for the tests.
     */
    @Configuration
    @Import(OssStorageProtocolResolver.class)
    static class TestConfiguration {

        @Bean
        public static OSS mockOSS() {
            OSS oss = mock(OSS.class);
            return oss;
        }
    }

}
