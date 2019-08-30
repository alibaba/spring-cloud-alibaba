package com.alibaba.alicloud.oss.resource;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author lich
 * @date 2019/8/29
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class OssStorageResourceTest {

    /**
     * Used to test exception messages and types.
     */
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Autowired
    private OSS oss;

    @Value("oss://aliyun-test-bucket/")
    private Resource bucketResource;

    @Value("oss://aliyun-test-bucket/myfilekey")
    private Resource remoteResource;

    public static byte[] generateRandomBytes(int blen) {
        byte[] array = new byte[blen];
        new Random().nextBytes(array);
        return array;
    }

    @Test
    public void testResourceType() {
        assertEquals(OssStorageResource.class, remoteResource.getClass());
        OssStorageResource ossStorageResource = (OssStorageResource)remoteResource;
        assertEquals("myfilekey", ossStorageResource.getFilename());
        assertFalse(ossStorageResource.isBucket());
    }

    @Test
    public void testValidObject() throws Exception {
        assertTrue(remoteResource.exists());
        OssStorageResource ossStorageResource = (OssStorageResource)remoteResource;
        assertTrue(ossStorageResource.bucketExists());
        assertEquals(4096L, remoteResource.contentLength());
        assertEquals("oss://aliyun-test-bucket/myfilekey", remoteResource.getURI().toString());
        assertEquals("myfilekey", remoteResource.getFilename());
    }

    @Test
    public void testBucketResource() throws Exception {
        assertTrue(bucketResource.exists());
        assertTrue(((OssStorageResource)this.bucketResource).isBucket());
        assertTrue(((OssStorageResource)this.bucketResource).bucketExists());
        assertEquals("oss://aliyun-test-bucket/", bucketResource.getURI().toString());
        assertEquals("aliyun-test-bucket", this.bucketResource.getFilename());
    }

    @Test
    public void testBucketNotEndingInSlash() {
        assertTrue(new OssStorageResource(this.oss, "oss://aliyun-test-bucket").isBucket());
    }

    @Test
    public void testSpecifyPathCorrect() {
        OssStorageResource ossStorageResource = new OssStorageResource (
            this.oss, "oss://aliyun-test-bucket/myfilekey", false);

        assertTrue(ossStorageResource.exists());
    }

    @Test
    public void testSpecifyBucketCorrect() {
        OssStorageResource ossStorageResource = new OssStorageResource(
            this.oss, "oss://aliyun-test-bucket", false);

        assertTrue(ossStorageResource.isBucket());
        assertEquals("aliyun-test-bucket", ossStorageResource.getBucket().getName());
        assertTrue(ossStorageResource.exists());
    }

    @Test
    public void testBucketOutputStream() throws IOException {
        this.expectedEx.expect(IllegalStateException.class);
        this.expectedEx.expectMessage("Cannot open an output stream to a bucket: 'oss://aliyun-test-bucket/'");
        ((WritableResource) this.bucketResource).getOutputStream();
    }

    @Test
    public void testBucketInputStream() throws IOException {
        this.expectedEx.expect(IllegalStateException.class);
        this.expectedEx.expectMessage("Cannot open an input stream to a bucket: 'oss://aliyun-test-bucket/'");
        this.bucketResource.getInputStream();
    }

    @Test
    public void testBucketContentLength() throws IOException {
        this.expectedEx.expect(FileNotFoundException.class);
        this.expectedEx.expectMessage("OSSObject not existed.");
        this.bucketResource.contentLength();
    }

    @Test
    public void testBucketFile() throws IOException {
        this.expectedEx.expect(UnsupportedOperationException.class);
        this.expectedEx.expectMessage("oss://aliyun-test-bucket/ cannot be resolved to absolute file path");
        this.bucketResource.getFile();
    }

    @Test
    public void testBucketLastModified() throws IOException {
        this.expectedEx.expect(FileNotFoundException.class);
        this.expectedEx.expectMessage("OSSObject not existed.");
        this.bucketResource.lastModified();
    }

    @Test
    public void testBucketResourceStatuses() {
        assertFalse(this.bucketResource.isOpen());
        assertFalse(((WritableResource) this.bucketResource).isWritable());
        assertTrue(this.bucketResource.exists());
    }

    @Test
    public void testWritable() throws Exception {
        assertTrue(this.remoteResource instanceof WritableResource);
        WritableResource writableResource = (WritableResource) this.remoteResource;
        assertTrue(writableResource.isWritable());
        writableResource.getOutputStream();
    }

    @Test
    public void testWritableOutputStream() throws Exception {
        String location = "oss://aliyun-test-bucket/test";
        OssStorageResource resource = new OssStorageResource(this.oss, location, true);
        OutputStream os = resource.getOutputStream();
        assertNotNull(os);

        byte[] randomBytes = generateRandomBytes(1203);
        String expectedString = new String(randomBytes);

        os.write(randomBytes);
        os.close();

        InputStream in = resource.getInputStream();

        byte[] result = StreamUtils.copyToByteArray(in);
        String actualString = new String(result);

        assertEquals(expectedString, actualString);
    }



    /**
     * Configuration for the tests.
     */
    @Configuration
    @Import(OssStorageProtocolResolver.class)
    static class TestConfiguration {

        @Bean
        public static OSS mockOSS() {
            DummyOssClient dummyOssStub = new DummyOssClient();
            OSS oss = mock(OSS.class);

            doAnswer(invocation ->
                dummyOssStub.putObject(
                    invocation.getArgument(0),
                    invocation.getArgument(1),
                    invocation.getArgument(2)
                ))
                .when(oss).putObject(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));

            doAnswer(invocation ->
                dummyOssStub.getOSSObject(
                    invocation.getArgument(0),
                    invocation.getArgument(1)
                ))
                .when(oss).getObject(Mockito.anyString(), Mockito.anyString());

            doAnswer(invocation -> dummyOssStub.bucketList())
                .when(oss).listBuckets();

            doAnswer(invocation -> dummyOssStub.createBucket(invocation.getArgument(0)))
                .when(oss).createBucket(Mockito.anyString());

            // prepare object
            dummyOssStub.createBucket("aliyun-test-bucket");

            byte[] content = generateRandomBytes(4096);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
            dummyOssStub.putObject("aliyun-test-bucket", "myfilekey", inputStream);

            return oss;
        }

    }

}
