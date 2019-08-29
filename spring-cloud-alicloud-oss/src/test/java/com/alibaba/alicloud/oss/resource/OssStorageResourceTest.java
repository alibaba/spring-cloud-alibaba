package com.alibaba.alicloud.oss.resource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import static org.junit.Assert.*;

/**
 * @author lich
 * @date 2019/8/29
 */

public class OssStorageResourceTest extends OssBaseTest {

    @Value("oss://aliyun-test-bucket/myfilekey")
    private Resource fileResource;

    @Test
    public void testResourceType() {
        assertEquals(OssStorageResource.class, fileResource.getClass());
        OssStorageResource ossStorageResource = (OssStorageResource)fileResource;
        assertEquals("myfilekey", ossStorageResource.getFilename());
        assertFalse(ossStorageResource.isBucket());
    }

}
