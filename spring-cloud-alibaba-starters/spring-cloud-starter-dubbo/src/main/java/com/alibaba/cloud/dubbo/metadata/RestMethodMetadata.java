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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.ResolvableType;

/**
 * Method Request Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestMethodMetadata {

	private MethodMetadata method;

	private RequestMetadata request;

	@JsonProperty("url-index")
	private Integer urlIndex;

	@JsonProperty("setBody-index")
	private Integer bodyIndex;

	@JsonProperty("header-map-index")
	private Integer headerMapIndex;

	@JsonProperty("query-map-index")
	private Integer queryMapIndex;

	@JsonProperty("query-map-encoded")
	private boolean queryMapEncoded;

	@JsonProperty("return-type")
	private String returnType;

	@JsonProperty("setBody-type")
	private String bodyType;

	@JsonProperty("index-to-name")
	private Map<Integer, Collection<String>> indexToName;

	@JsonProperty("form-params")
	private List<String> formParams;

	@JsonProperty("index-to-encoded")
	private Map<Integer, Boolean> indexToEncoded;

	public RestMethodMetadata() {
	}

	public RestMethodMetadata(feign.MethodMetadata methodMetadata) {
		this.request = new RequestMetadata(methodMetadata.template());
		this.urlIndex = methodMetadata.urlIndex();
		this.bodyIndex = methodMetadata.bodyIndex();
		this.headerMapIndex = methodMetadata.headerMapIndex();
		this.queryMapEncoded = methodMetadata.queryMapEncoded();
		this.queryMapEncoded = methodMetadata.queryMapEncoded();
		this.returnType = getClassName(methodMetadata.returnType());
		this.bodyType = getClassName(methodMetadata.bodyType());
		this.indexToName = methodMetadata.indexToName();
		this.formParams = methodMetadata.formParams();
		this.indexToEncoded = methodMetadata.indexToEncoded();
	}

	public MethodMetadata getMethod() {
		return method;
	}

	public void setMethod(MethodMetadata method) {
		this.method = method;
	}

	public RequestMetadata getRequest() {
		return request;
	}

	public void setRequest(RequestMetadata request) {
		this.request = request;
	}

	public Map<Integer, Collection<String>> getIndexToName() {
		return indexToName;
	}

	public void setIndexToName(Map<Integer, Collection<String>> indexToName) {
		this.indexToName = indexToName;
	}

	public Integer getUrlIndex() {
		return urlIndex;
	}

	public void setUrlIndex(Integer urlIndex) {
		this.urlIndex = urlIndex;
	}

	public Integer getBodyIndex() {
		return bodyIndex;
	}

	public void setBodyIndex(Integer bodyIndex) {
		this.bodyIndex = bodyIndex;
	}

	public Integer getHeaderMapIndex() {
		return headerMapIndex;
	}

	public void setHeaderMapIndex(Integer headerMapIndex) {
		this.headerMapIndex = headerMapIndex;
	}

	public Integer getQueryMapIndex() {
		return queryMapIndex;
	}

	public void setQueryMapIndex(Integer queryMapIndex) {
		this.queryMapIndex = queryMapIndex;
	}

	public boolean isQueryMapEncoded() {
		return queryMapEncoded;
	}

	public void setQueryMapEncoded(boolean queryMapEncoded) {
		this.queryMapEncoded = queryMapEncoded;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	public List<String> getFormParams() {
		return formParams;
	}

	public void setFormParams(List<String> formParams) {
		this.formParams = formParams;
	}

	public Map<Integer, Boolean> getIndexToEncoded() {
		return indexToEncoded;
	}

	public void setIndexToEncoded(Map<Integer, Boolean> indexToEncoded) {
		this.indexToEncoded = indexToEncoded;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RestMethodMetadata)) {
			return false;
		}
		RestMethodMetadata that = (RestMethodMetadata) o;
		return queryMapEncoded == that.queryMapEncoded
				&& Objects.equals(method, that.method)
				&& Objects.equals(request, that.request)
				&& Objects.equals(urlIndex, that.urlIndex)
				&& Objects.equals(bodyIndex, that.bodyIndex)
				&& Objects.equals(headerMapIndex, that.headerMapIndex)
				&& Objects.equals(queryMapIndex, that.queryMapIndex)
				&& Objects.equals(returnType, that.returnType)
				&& Objects.equals(bodyType, that.bodyType)
				&& Objects.equals(indexToName, that.indexToName)
				&& Objects.equals(formParams, that.formParams)
				&& Objects.equals(indexToEncoded, that.indexToEncoded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method, request, urlIndex, bodyIndex, headerMapIndex,
				queryMapIndex, queryMapEncoded, returnType, bodyType, indexToName,
				formParams, indexToEncoded);
	}

	private String getClassName(Type type) {
		if (type == null) {
			return null;
		}
		ResolvableType resolvableType = ResolvableType.forType(type);
		return resolvableType.resolve().getName();
	}

	@Override
	public String toString() {
		return "RestMethodMetadata{" + "method=" + method + ", request=" + request
				+ ", urlIndex=" + urlIndex + ", bodyIndex=" + bodyIndex
				+ ", headerMapIndex=" + headerMapIndex + ", queryMapIndex="
				+ queryMapIndex + ", queryMapEncoded=" + queryMapEncoded
				+ ", returnType='" + returnType + '\'' + ", bodyType='" + bodyType + '\''
				+ ", indexToName=" + indexToName + ", formParams=" + formParams
				+ ", indexToEncoded=" + indexToEncoded + '}';
	}

}
