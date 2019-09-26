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

package com.alibaba.cloud.stream.binder.rocketmq.support;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ClassUtils;

/**
 * jackson header mapper for RocketMQ. Header types are added to a special header
 * {@link #JSON_TYPES}.
 *
 * @author caotc
 * @since 2.1.1.RELEASE
 */
public class JacksonRocketMQHeaderMapper extends AbstractRocketMQHeaderMapper {

	private final static Logger log = LoggerFactory
			.getLogger(JacksonRocketMQHeaderMapper.class);

	private static final List<String> DEFAULT_TRUSTED_PACKAGES = Arrays
			.asList("java.lang", "java.net", "java.util", "org.springframework.util");

	/**
	 * Header name for java types of other headers.
	 */
	public static final String JSON_TYPES = "spring_json_header_types";

	private final ObjectMapper objectMapper;

	private final Set<String> trustedPackages = new LinkedHashSet<>(
			DEFAULT_TRUSTED_PACKAGES);

	public JacksonRocketMQHeaderMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public JacksonRocketMQHeaderMapper(Charset charset, ObjectMapper objectMapper) {
		super(charset);
		this.objectMapper = objectMapper;
	}

	@Override
	public Map<String, String> fromHeaders(MessageHeaders headers) {
		final Map<String, String> target = new HashMap<>();
		final Map<String, String> jsonHeaders = new HashMap<>();
		headers.forEach((key, value) -> {
			if (matches(key)) {
				if (value instanceof String) {
					target.put(key, (String) value);
				}
				else {
					try {
						String className = value.getClass().getName();
						target.put(key, objectMapper.writeValueAsString(value));
						jsonHeaders.put(key, className);
					}
					catch (Exception e) {
						log.debug("Could not map " + key + " with type "
								+ value.getClass().getName(), e);
					}
				}
			}
		});
		if (jsonHeaders.size() > 0) {
			try {
				target.put(JSON_TYPES, objectMapper.writeValueAsString(jsonHeaders));
			}
			catch (IllegalStateException | JsonProcessingException e) {
				log.error("Could not add json types header", e);
			}
		}
		return target;
	}

	@Override
	public MessageHeaders toHeaders(Map<String, String> source) {
		final Map<String, Object> target = new HashMap<>();
		final Map<String, String> jsonTypes = decodeJsonTypes(source);
		source.forEach((key, value) -> {
			if (matches(key) && !(key.equals(JSON_TYPES))) {
				if (jsonTypes != null && jsonTypes.containsKey(key)) {
					Class<?> type = Object.class;
					String requestedType = jsonTypes.get(key);
					boolean trusted = trusted(requestedType);
					if (trusted) {
						try {
							type = ClassUtils.forName(requestedType, null);
						}
						catch (Exception e) {
							log.error("Could not load class for header: " + key, e);
						}
					}

					if (trusted) {
						try {
							Object val = decodeValue(value, type);
							target.put(key, val);
						}
						catch (IOException e) {
							log.error("Could not decode json type: " + value
									+ " for key: " + key, e);
							target.put(key, value);
						}
					}
					else {
						target.put(key, new NonTrustedHeaderType(value, requestedType));
					}
				}
				else {
					target.put(key, value);
				}
			}
		});
		return new MessageHeaders(target);
	}

	/**
	 * @param packagesToTrust the packages to trust.
	 * @see #addTrustedPackages(Collection)
	 */
	public void addTrustedPackages(String... packagesToTrust) {
		if (Objects.nonNull(packagesToTrust)) {
			addTrustedPackages(Arrays.asList(packagesToTrust));
		}
	}

	/**
	 * Add packages to the trusted packages list (default {@code java.util, java.lang})
	 * used when constructing objects from JSON. If any of the supplied packages is
	 * {@code "*"}, all packages are trusted. If a class for a non-trusted package is
	 * encountered, the header is returned to the application with value of type
	 * {@link NonTrustedHeaderType}.
	 * @param packagesToTrust the packages to trust.
	 */
	public void addTrustedPackages(Collection<String> packagesToTrust) {
		if (packagesToTrust != null) {
			for (String whiteList : packagesToTrust) {
				if ("*".equals(whiteList)) {
					this.trustedPackages.clear();
					break;
				}
				else {
					this.trustedPackages.add(whiteList);
				}
			}
		}
	}

	public Set<String> getTrustedPackages() {
		return this.trustedPackages;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	private Object decodeValue(String jsonString, Class<?> type)
			throws IOException, LinkageError {
		Object value = objectMapper.readValue(jsonString, type);
		if (type.equals(NonTrustedHeaderType.class)) {
			// Upstream NTHT propagated; may be trusted here...
			NonTrustedHeaderType nth = (NonTrustedHeaderType) value;
			if (trusted(nth.getUntrustedType())) {
				try {
					value = objectMapper.readValue(nth.getHeaderValue(),
							ClassUtils.forName(nth.getUntrustedType(), null));
				}
				catch (Exception e) {
					log.error("Could not decode header: " + nth, e);
				}
			}
		}
		return value;
	}

	@Nullable
	private Map<String, String> decodeJsonTypes(Map<String, String> source) {
		if (source.containsKey(JSON_TYPES)) {
			String value = source.get(JSON_TYPES);
			try {
				return objectMapper.readValue(value,
						new TypeReference<Map<String, String>>() {
						});
			}
			catch (IOException e) {
				log.error("Could not decode json types: " + value, e);
			}
		}
		return null;
	}

	protected boolean trusted(String requestedType) {
		if (requestedType.equals(NonTrustedHeaderType.class.getName())) {
			return true;
		}
		if (!this.trustedPackages.isEmpty()) {
			int lastDot = requestedType.lastIndexOf('.');
			if (lastDot < 0) {
				return false;
			}
			String packageName = requestedType.substring(0, lastDot);
			for (String trustedPackage : this.trustedPackages) {
				if (packageName.equals(trustedPackage)
						|| packageName.startsWith(trustedPackage + ".")) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Represents a header that could not be decoded due to an untrusted type.
	 */
	public static class NonTrustedHeaderType {

		private String headerValue;

		private String untrustedType;

		public NonTrustedHeaderType() {
			super();
		}

		NonTrustedHeaderType(String headerValue, String untrustedType) {
			this.headerValue = headerValue;
			this.untrustedType = untrustedType;
		}

		public void setHeaderValue(String headerValue) {
			this.headerValue = headerValue;
		}

		public String getHeaderValue() {
			return this.headerValue;
		}

		public void setUntrustedType(String untrustedType) {
			this.untrustedType = untrustedType;
		}

		public String getUntrustedType() {
			return this.untrustedType;
		}

		@Override
		public String toString() {
			return "NonTrustedHeaderType [headerValue=" + headerValue + ", untrustedType="
					+ this.untrustedType + "]";
		}

	}

}
