/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.oss.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;

/**
 * Implements {@link Resource} for reading and writing objects in Aliyun Object Storage
 * Service (OSS). An instance of this class represents a handle to a bucket or an OSSObject.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see OSS
 * @see Bucket
 * @see OSSObject
 */
public class OSSStorageResource implements Resource {

	private final OSS oss;
	private final String bucketName;
	private final String objectKey;
	private final URI location;

	public OSSStorageResource(OSS oss, String location) {
		Assert.notNull(oss, "Object Storage Service can not be null");
		Assert.isTrue(location.startsWith(OSSStorageProtocolResolver.PROTOCOL),
				"Location must start with " + OSSStorageProtocolResolver.PROTOCOL);
		this.oss = oss;
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
		return new OSSStorageResource(this.oss,
				this.location.resolve(relativePath).toString());
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
				.get();
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

}
