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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;

import static com.alibaba.alicloud.oss.OssConstants.OSS_TASK_EXECUTOR_BEAN_NAME;

/**
 * Implements {@link Resource} for reading and writing objects in Aliyun Object Storage
 * Service (OSS). An instance of this class represents a handle to a bucket or an
 * OSSObject.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see OSS
 * @see Bucket
 * @see OSSObject
 */
public class OssStorageResource implements WritableResource {

	private static final Logger logger = LoggerFactory
			.getLogger(OssStorageResource.class);

	private static final String MESSAGE_KEY_NOT_EXIST = "The specified key does not exist.";

	private final OSS oss;

	private final String bucketName;

	private final String objectKey;

	private final URI location;

	private final boolean autoCreateFiles;

	private final ExecutorService ossTaskExecutor;

	private final ConfigurableListableBeanFactory beanFactory;

	public OssStorageResource(OSS oss, String location,
			ConfigurableListableBeanFactory beanFactory) {
		this(oss, location, beanFactory, false);
	}

	public OssStorageResource(OSS oss, String location,
			ConfigurableListableBeanFactory beanFactory, boolean autoCreateFiles) {
		Assert.notNull(oss, "Object Storage Service can not be null");
		Assert.isTrue(location.startsWith(OssStorageProtocolResolver.PROTOCOL),
				"Location must start with " + OssStorageProtocolResolver.PROTOCOL);
		this.oss = oss;
		this.autoCreateFiles = autoCreateFiles;
		this.beanFactory = beanFactory;
		try {
			URI locationUri = new URI(location);
			this.bucketName = locationUri.getAuthority();

			if (locationUri.getPath() != null && locationUri.getPath().length() > 1) {
				this.objectKey = locationUri.getPath().substring(1);
			}
			else {
				this.objectKey = null;
			}
			this.location = locationUri;
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid location: " + location, e);
		}

		this.ossTaskExecutor = this.beanFactory.getBean(OSS_TASK_EXECUTOR_BEAN_NAME,
				ExecutorService.class);
	}

	public boolean isAutoCreateFiles() {
		return this.autoCreateFiles;
	}

	@Override
	public boolean exists() {
		try {
			return isBucket() ? getBucket() != null : getOSSObject() != null;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Since the oss: protocol will normally not have a URL stream handler registered,
	 * this method will always throw a {@link java.net.MalformedURLException}.
	 * @return The URL for the OSS resource, if a URL stream handler is registered for the
	 * oss protocol.
	 */
	@Override
	public URL getURL() throws IOException {
		return this.location.toURL();
	}

	@Override
	public URI getURI() throws IOException {
		return this.location;
	}

	@Override
	public File getFile() throws IOException {
		throw new UnsupportedOperationException(
				getDescription() + " cannot be resolved to absolute file path");
	}

	@Override
	public long contentLength() throws IOException {
		assertExisted();
		if (isBucket()) {
			throw new FileNotFoundException("OSSObject not existed.");
		}
		return getOSSObject().getObjectMetadata().getContentLength();
	}

	@Override
	public long lastModified() throws IOException {
		assertExisted();
		if (isBucket()) {
			throw new FileNotFoundException("OSSObject not existed.");
		}
		return getOSSObject().getObjectMetadata().getLastModified().getTime();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return new OssStorageResource(this.oss,
				this.location.resolve(relativePath).toString(), this.beanFactory);
	}

	@Override
	public String getFilename() {
		return isBucket() ? this.bucketName : this.objectKey;
	}

	@Override
	public String getDescription() {
		return this.location.toString();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		assertExisted();
		if (isBucket()) {
			throw new IllegalStateException(
					"Cannot open an input stream to a bucket: '" + this.location + "'");
		}
		else {
			return getOSSObject().getObjectContent();
		}
	}

	/**
	 * Returns the {@link Bucket} associated with the resource.
	 * @return the bucket if it exists, or null otherwise
	 */
	public Bucket getBucket() {
		return this.oss.listBuckets().stream()
				.filter(bucket -> bucket.getName().equals(this.bucketName)).findFirst()
				.orElse(null);
	}

	/**
	 * Checks for the existence of the {@link Bucket} associated with the resource.
	 * @return true if the bucket exists
	 */
	public boolean bucketExists() {
		return getBucket() != null;
	}

	/**
	 * Gets the underlying resource object in Aliyun Object Storage Service.
	 * @return The resource object, will be null if it does not exist in Aliyun Object
	 * Storage Service.
	 * @throws OSSException it is thrown upon error when accessing OSS
	 * @throws ClientException it is the one thrown by the client side when accessing OSS
	 */
	public OSSObject getOSSObject() {
		return this.oss.getObject(this.bucketName, this.objectKey);
	}

	/**
	 * Check if this resource references a bucket and not a blob.
	 * @return if the resource is bucket
	 */
	public boolean isBucket() {
		return this.objectKey == null;
	}

	private void assertExisted() throws FileNotFoundException {
		if (!exists()) {
			throw new FileNotFoundException("Bucket or OSSObject not existed.");
		}
	}

	/**
	 * create a bucket.
	 * @return OSS Bucket
	 */
	public Bucket createBucket() {
		return this.oss.createBucket(this.bucketName);
	}

	@Override
	public boolean isWritable() {
		return !isBucket() && (this.autoCreateFiles || exists());
	}

	/**
	 * acquire an OutputStream for write. Note: please close the stream after writing is
	 * done
	 * @return OutputStream of OSS resource
	 * @throws IOException throw by oss operation
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (isBucket()) {
			throw new IllegalStateException(
					"Cannot open an output stream to a bucket: '" + getURI() + "'");
		}
		else {
			OSSObject ossObject;

			try {
				ossObject = this.getOSSObject();
			}
			catch (OSSException ex) {
				if (ex.getMessage() != null
						&& ex.getMessage().startsWith(MESSAGE_KEY_NOT_EXIST)) {
					ossObject = null;
				}
				else {
					throw ex;
				}
			}

			if (ossObject == null) {
				if (!this.autoCreateFiles) {
					throw new FileNotFoundException(
							"The object was not found: " + getURI());
				}

			}

			PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream(in);

			ossTaskExecutor.submit(() -> {
				try {
					OssStorageResource.this.oss.putObject(bucketName, objectKey, in);
				}
				catch (Exception ex) {
					logger.error("Failed to put object", ex);
				}
			});

			return out;
		}

	}

}
