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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;

import org.springframework.util.StreamUtils;

/**
 * @author lich
 */
public class DummyOssClient {

	private Map<String, byte[]> storeMap = new ConcurrentHashMap<>();

	private Map<String, Bucket> bucketSet = new HashMap<>();

	public String getStoreKey(String bucketName, String objectKey) {
		return String.join(".", bucketName, objectKey);
	}

	public PutObjectResult putObject(String bucketName, String objectKey,
			InputStream inputStream) {

		try {
			byte[] result = StreamUtils.copyToByteArray(inputStream);
			storeMap.put(getStoreKey(bucketName, objectKey), result);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return new PutObjectResult();
	}

	public OSSObject getOSSObject(String bucketName, String objectKey) {
		byte[] value = storeMap.get(this.getStoreKey(bucketName, objectKey));
		if (value == null) {
			return null;
		}
		OSSObject ossObject = new OSSObject();
		ossObject.setBucketName(bucketName);
		ossObject.setKey(objectKey);
		InputStream inputStream = new ByteArrayInputStream(value);
		ossObject.setObjectContent(inputStream);

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(value.length);
		ossObject.setObjectMetadata(objectMetadata);

		return ossObject;
	}

	public Bucket createBucket(String bucketName) {
		if (bucketSet.containsKey(bucketName)) {
			return bucketSet.get(bucketName);
		}
		Bucket bucket = new Bucket();
		bucket.setCreationDate(new Date());
		bucket.setName(bucketName);
		bucketSet.put(bucketName, bucket);
		return bucket;
	}

	public List<Bucket> bucketList() {
		return new ArrayList<>(bucketSet.values());
	}

}
