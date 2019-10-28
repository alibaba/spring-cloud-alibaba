/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.alicloud.oss.resource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aliyun.oss.OSS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import static com.alibaba.alicloud.oss.OssConstants.OSS_TASK_EXECUTOR_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author lich
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
	private ConfigurableListableBeanFactory beanFactory;

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
		assertThat(remoteResource.getClass()).isEqualTo(OssStorageResource.class);
		OssStorageResource ossStorageResource = (OssStorageResource) remoteResource;
		assertThat(ossStorageResource.getFilename()).isEqualTo("myfilekey");
		assertThat(ossStorageResource.isBucket()).isEqualTo(false);
	}

	@Test
	public void testValidObject() throws Exception {
		assertThat(remoteResource.exists()).isEqualTo(true);
		OssStorageResource ossStorageResource = (OssStorageResource) remoteResource;
		assertThat(ossStorageResource.bucketExists()).isEqualTo(true);
		assertThat(remoteResource.contentLength()).isEqualTo(4096L);
		assertThat(remoteResource.getURI().toString())
				.isEqualTo("oss://aliyun-test-bucket/myfilekey");
		assertThat(remoteResource.getFilename()).isEqualTo("myfilekey");
	}

	@Test
	public void testBucketResource() throws Exception {
		assertThat(bucketResource.exists()).isEqualTo(true);
		assertThat(((OssStorageResource) this.bucketResource).isBucket()).isEqualTo(true);
		assertThat(((OssStorageResource) this.bucketResource).bucketExists())
				.isEqualTo(true);
		assertThat(bucketResource.getURI().toString())
				.isEqualTo("oss://aliyun-test-bucket/");
		assertThat(this.bucketResource.getFilename()).isEqualTo("aliyun-test-bucket");
	}

	@Test
	public void testBucketNotEndingInSlash() {
		assertThat(
				new OssStorageResource(this.oss, "oss://aliyun-test-bucket", beanFactory)
						.isBucket()).isEqualTo(true);
	}

	@Test
	public void testSpecifyPathCorrect() {
		OssStorageResource ossStorageResource = new OssStorageResource(this.oss,
				"oss://aliyun-test-bucket/myfilekey", beanFactory, false);
		assertThat(ossStorageResource.exists()).isEqualTo(true);
	}

	@Test
	public void testSpecifyBucketCorrect() {
		OssStorageResource ossStorageResource = new OssStorageResource(this.oss,
				"oss://aliyun-test-bucket", beanFactory, false);

		assertThat(ossStorageResource.isBucket()).isEqualTo(true);
		assertThat(ossStorageResource.getBucket().getName())
				.isEqualTo("aliyun-test-bucket");
		assertThat(ossStorageResource.exists()).isEqualTo(true);
	}

	@Test
	public void testBucketOutputStream() throws IOException {
		this.expectedEx.expect(IllegalStateException.class);
		this.expectedEx.expectMessage(
				"Cannot open an output stream to a bucket: 'oss://aliyun-test-bucket/'");
		((WritableResource) this.bucketResource).getOutputStream();
	}

	@Test
	public void testBucketInputStream() throws IOException {
		this.expectedEx.expect(IllegalStateException.class);
		this.expectedEx.expectMessage(
				"Cannot open an input stream to a bucket: 'oss://aliyun-test-bucket/'");
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
		this.expectedEx.expectMessage(
				"oss://aliyun-test-bucket/ cannot be resolved to absolute file path");
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
		assertThat(this.bucketResource.isOpen()).isEqualTo(false);
		assertThat(((WritableResource) this.bucketResource).isWritable())
				.isEqualTo(false);
		assertThat(this.bucketResource.exists()).isEqualTo(true);
	}

	@Test
	public void testWritable() throws Exception {
		assertThat(this.remoteResource instanceof WritableResource).isEqualTo(true);
		WritableResource writableResource = (WritableResource) this.remoteResource;
		assertThat(writableResource.isWritable()).isEqualTo(true);
		writableResource.getOutputStream();
	}

	@Test
	public void testWritableOutputStream() throws Exception {
		String location = "oss://aliyun-test-bucket/test";
		OssStorageResource resource = new OssStorageResource(this.oss, location,
				beanFactory, true);
		OutputStream os = resource.getOutputStream();
		assertThat(os).isNotNull();

		byte[] randomBytes = generateRandomBytes(1203);
		String expectedString = new String(randomBytes);

		os.write(randomBytes);
		os.close();

		InputStream in = resource.getInputStream();

		byte[] result = StreamUtils.copyToByteArray(in);
		String actualString = new String(result);

		assertThat(actualString).isEqualTo(expectedString);
	}

	@Test
	public void testCreateBucket() {
		String location = "oss://my-new-test-bucket/";
		OssStorageResource resource = new OssStorageResource(this.oss, location,
				beanFactory, true);

		resource.createBucket();

		assertThat(resource.bucketExists()).isEqualTo(true);
	}

	/**
	 * Configuration for the tests.
	 */
	@Configuration
	@Import(OssStorageProtocolResolver.class)
	static class TestConfiguration {

		@Bean(name = OSS_TASK_EXECUTOR_BEAN_NAME)
		@ConditionalOnMissingBean
		public ExecutorService ossTaskExecutor() {
			return new ThreadPoolExecutor(8, 128, 60, TimeUnit.SECONDS,
					new SynchronousQueue<>());
		}

		@Bean
		public static OSS mockOSS() {
			DummyOssClient dummyOssStub = new DummyOssClient();
			OSS oss = mock(OSS.class);

			doAnswer(invocation -> dummyOssStub.putObject(invocation.getArgument(0),
					invocation.getArgument(1), invocation.getArgument(2))).when(oss)
							.putObject(Mockito.anyString(), Mockito.anyString(),
									Mockito.any(InputStream.class));

			doAnswer(invocation -> dummyOssStub.getOSSObject(invocation.getArgument(0),
					invocation.getArgument(1))).when(oss).getObject(Mockito.anyString(),
							Mockito.anyString());

			doAnswer(invocation -> dummyOssStub.bucketList()).when(oss).listBuckets();

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
