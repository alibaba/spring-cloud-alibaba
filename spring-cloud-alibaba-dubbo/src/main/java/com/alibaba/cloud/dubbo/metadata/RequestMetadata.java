/*
 * Copyright (C) 2018 the original author or authors.
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

import static com.alibaba.cloud.dubbo.http.util.HttpUtils.normalizePath;
import static org.springframework.http.MediaType.parseMediaTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import feign.RequestTemplate;

/**
 * Request Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RequestMetadata {

	private String method;

	private String path;

	@JsonProperty("params")
	private MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

	@JsonProperty("headers")
	private HttpHeaders headers = new HttpHeaders();

	private Set<String> consumes = new LinkedHashSet<>();

	private Set<String> produces = new LinkedHashSet<>();

	public RequestMetadata() {
	}

	public RequestMetadata(RequestTemplate requestTemplate) {
		setMethod(requestTemplate.method());
		setPath(requestTemplate.url());
		params(requestTemplate.queries());
		headers(requestTemplate.headers());
	}

	/**
	 * Get the best matched {@link RequestMetadata} via specified {@link RequestMetadata}
	 *
	 * @param requestMetadataMap the source of {@link NavigableMap}
	 * @param requestMetadata the match object
	 * @return if not matched, return <code>null</code>
	 */
	public static RequestMetadata getBestMatch(
			NavigableMap<RequestMetadata, RequestMetadata> requestMetadataMap,
			RequestMetadata requestMetadata) {

		RequestMetadata key = requestMetadata;

		RequestMetadata result = requestMetadataMap.get(key);

		if (result == null) {
			SortedMap<RequestMetadata, RequestMetadata> headMap = requestMetadataMap
					.headMap(key, true);
			result = headMap.isEmpty() ? null : requestMetadataMap.get(headMap.lastKey());
		}

		return result;
	}

	private static void add(String key, String value,
			MultiValueMap<String, String> destination) {
		destination.add(key, value);
	}

	private static <T extends Collection<String>> void addAll(Map<String, T> source,
			MultiValueMap<String, String> destination) {
		for (Map.Entry<String, T> entry : source.entrySet()) {
			String key = entry.getKey();
			for (String value : entry.getValue()) {
				add(key, value, destination);
			}
		}
	}

	private static void mediaTypes(HttpHeaders httpHeaders, String headerName,
			Collection<String> destination) {
		List<String> value = httpHeaders.get(headerName);
		List<MediaType> mediaTypes = parseMediaTypes(value);
		destination.addAll(toMediaTypeValues(mediaTypes));
	}

	private static List<String> toMediaTypeValues(List<MediaType> mediaTypes) {
		List<String> list = new ArrayList<>(mediaTypes.size());
		for (MediaType mediaType : mediaTypes) {
			list.add(mediaType.toString());
		}
		return list;
	}

	private static List<MediaType> toMediaTypes(Collection<String> mediaTypeValues) {
		if (mediaTypeValues.isEmpty()) {
			return Collections.singletonList(MediaType.ALL);
		}
		return parseMediaTypes(new LinkedList<>(mediaTypeValues));
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = normalizePath(path);
	}

	public MultiValueMap<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, List<String>> params) {
		params(params);
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		headers(headers);
	}

	public Set<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(Set<String> consumes) {
		this.consumes = consumes;
	}

	public Set<String> getProduces() {
		return produces;
	}

	public void setProduces(Set<String> produces) {
		this.produces = produces;
	}

	// @JsonIgnore properties
	@JsonIgnore
	public Set<String> getParamNames() {
		return params.keySet();
	}

	@JsonIgnore
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	@JsonIgnore
	public List<MediaType> getConsumeMediaTypes() {
		return toMediaTypes(consumes);
	}

	@JsonIgnore
	public List<MediaType> getProduceMediaTypes() {
		return toMediaTypes(produces);
	}

	public String getParameter(String name) {
		return this.params.getFirst(name);
	}

	public String getHeader(String name) {
		return this.headers.getFirst(name);
	}

	public RequestMetadata addParam(String name, String value) {
		add(name, value, this.params);
		return this;
	}

	public RequestMetadata addHeader(String name, String value) {
		add(name, value, this.headers);
		return this;
	}

	private <T extends Collection<String>> RequestMetadata params(Map<String, T> params) {
		addAll(params, this.params);
		return this;
	}

	private <T extends Collection<String>> RequestMetadata headers(
			Map<String, T> headers) {
		if (!CollectionUtils.isEmpty(headers)) {
			HttpHeaders httpHeaders = new HttpHeaders();
			// Add all headers
			addAll(headers, httpHeaders);
			// Handles "Content-Type" and "Accept" headers if present
			mediaTypes(httpHeaders, HttpHeaders.CONTENT_TYPE, this.consumes);
			mediaTypes(httpHeaders, HttpHeaders.ACCEPT, this.produces);
			this.headers.putAll(httpHeaders);
		}
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RequestMetadata)) {
			return false;
		}
		RequestMetadata that = (RequestMetadata) o;
		return Objects.equals(method, that.method) && Objects.equals(path, that.path)
				&& Objects.equals(consumes, that.consumes)
				&& Objects.equals(produces, that.produces) &&
				// Metadata should not compare the values
				Objects.equals(getParamNames(), that.getParamNames())
				&& Objects.equals(getHeaderNames(), that.getHeaderNames());

	}

	@Override
	public int hashCode() {
		// The values of metadata should not use for the hashCode() method
		return Objects.hash(method, path, consumes, produces, getParamNames(),
				getHeaderNames());
	}

	@Override
	public String toString() {
		return "RequestMetadata{" + "method='" + method + '\'' + ", path='" + path + '\''
				+ ", params=" + params + ", headers=" + headers + ", consumes=" + consumes
				+ ", produces=" + produces + '}';
	}
}
