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

package com.alibaba.cloud.dubbo.metadata;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;

import static com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository.EXPORTED_SERVICES_REVISION_PROPERTY_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Copy from org.apache.dubbo.metadata.RevisionResolver.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public final class RevisionResolver {

	/**
	 * The param key in url.
	 */
	public static final String SCA_REVSION_KEY = "sca_revision";

	private static final String EMPTY_REVISION = "0";

	private static final Logger logger = LoggerFactory.getLogger(RevisionResolver.class);

	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static MessageDigest mdInst;

	static {
		try {
			mdInst = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			logger.error("Failed to calculate metadata revision", e);
		}
	}

	private RevisionResolver() {

	}

	public static String getEmptyRevision() {
		return EMPTY_REVISION;
	}

	public static String calRevision(String metadata) {
		mdInst.update(metadata.getBytes(UTF_8));
		byte[] md5 = mdInst.digest();

		int j = md5.length;
		char[] str = new char[j * 2];
		int k = 0;
		for (byte byte0 : md5) {
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}

	public static String getRevision(ServiceInstance instance) {
		Map<String, String> metadata = instance.getMetadata();
		String revision = metadata.get(EXPORTED_SERVICES_REVISION_PROPERTY_NAME);

		if (revision == null) {
			revision = RevisionResolver.getEmptyRevision();
		}
		return revision;
	}

}
