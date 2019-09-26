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

package com.alibaba.cloud.dubbo.http.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import static org.springframework.util.StringUtils.delimitedListToStringArray;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * Http Utilities class.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class HttpUtils {

	/**
	 * Charset value for Http.
	 */
	private static final String UTF_8 = "UTF-8";

	/**
	 * Equal operator for Http.
	 */
	private static final String EQUAL = "=";

	/**
	 * And operator for Http.
	 */
	private static final String AND = "&";

	/**
	 * Semicolon operator for Http.
	 */
	private static final String SEMICOLON = ";";

	/**
	 * Question Mark operation for Http.
	 */
	private static final String QUESTION_MASK = "?";

	/**
	 * The empty value.
	 */
	private static final String EMPTY_VALUE = "";

	/**
	 * Normalize path:
	 * <ol>
	 * <li>To remove query string if presents</li>
	 * <li>To remove duplicated slash("/") if exists.</li>
	 * </ol>
	 * @param path path to be normalized
	 * @return a normalized path if required
	 */
	public static String normalizePath(String path) {
		if (!hasText(path)) {
			return path;
		}
		String normalizedPath = path;
		int index = normalizedPath.indexOf(QUESTION_MASK);
		if (index > -1) {
			normalizedPath = normalizedPath.substring(0, index);
		}
		return StringUtils.replace(normalizedPath, "//", "/");
	}

	/**
	 * Get Parameters from the specified {@link HttpRequest request}.
	 * @param request the specified {@link HttpRequest request}
	 * @return map of parameters
	 */
	public static MultiValueMap<String, String> getParameters(HttpRequest request) {
		URI uri = request.getURI();
		return getParameters(uri.getQuery());
	}

	/**
	 * Get Parameters from the specified query string.
	 * <p>
	 * @param queryString The query string
	 * @return The query parameters
	 */
	public static MultiValueMap<String, String> getParameters(String queryString) {
		return getParameters(delimitedListToStringArray(queryString, AND));
	}

	/**
	 * Get Parameters from the specified pairs of name-value.
	 * <p>
	 * @param pairs The pairs of name-value
	 * @return The query parameters
	 */
	public static MultiValueMap<String, String> getParameters(Iterable<String> pairs) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		if (pairs != null) {
			for (String pair : pairs) {
				String[] nameAndValue = delimitedListToStringArray(pair, EQUAL);
				String name = decode(nameAndValue[0]);
				String value = nameAndValue.length < 2 ? null : nameAndValue[1];
				value = decode(value);
				addParam(parameters, name, value);
			}
		}
		return parameters;
	}

	/**
	 * Get Parameters from the specified pairs of name-value.
	 * <p>
	 * @param pairs The pairs of name-value
	 * @return The query parameters
	 */
	public static MultiValueMap<String, String> getParameters(String... pairs) {
		return getParameters(Arrays.asList(pairs));
	}

	// /**
	// * Parse a read-only {@link MultiValueMap} of {@link HttpCookie} from {@link
	// HttpHeaders}
	// *
	// * @param httpHeaders {@link HttpHeaders}
	// * @return non-null, the key is a cookie name , the value is {@link HttpCookie}
	// */
	// public static MultiValueMap<String, HttpCookie> parseCookies(HttpHeaders
	// httpHeaders) {
	//
	// String cookie = httpHeaders.getFirst(COOKIE);
	//
	// String[] cookieNameAndValues = StringUtils.delimitedListToStringArray(cookie,
	// SEMICOLON);
	//
	// MultiValueMap<String, HttpCookie> cookies = new
	// LinkedMultiValueMap<>(cookieNameAndValues.length);
	//
	// for (String cookeNameAndValue : cookieNameAndValues) {
	// String[] nameAndValue =
	// delimitedListToStringArray(trimWhitespace(cookeNameAndValue), EQUAL);
	// String name = nameAndValue[0];
	// String value = nameAndValue.length < 2 ? null : nameAndValue[1];
	// HttpCookie httpCookie = new HttpCookie(name, value);
	// cookies.add(name, httpCookie);
	// }
	//
	// return cookies;
	// }

	/**
	 * To the name and value line sets.
	 * @param nameAndValuesMap {@link MultiValueMap} the map of name and values
	 * @return non-null
	 */
	public static Set<String> toNameAndValuesSet(
			Map<String, List<String>> nameAndValuesMap) {
		Set<String> nameAndValues = new LinkedHashSet<>();
		for (Map.Entry<String, List<String>> entry : nameAndValuesMap.entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			for (String value : values) {
				String nameAndValue = name + EQUAL + value;
				nameAndValues.add(nameAndValue);
			}
		}
		return nameAndValues;
	}

	public static String[] toNameAndValues(Map<String, List<String>> nameAndValuesMap) {
		return toNameAndValuesSet(nameAndValuesMap).toArray(new String[0]);
	}

	/**
	 * Generate a string of query string from the specified request parameters
	 * {@link Map}.
	 * @param params the specified request parameters {@link Map}
	 * @return non-null
	 */
	public static String toQueryString(Map<String, List<String>> params) {
		StringBuilder builder = new StringBuilder();
		for (String line : toNameAndValuesSet(params)) {
			builder.append(line).append(AND);
		}
		return builder.toString();
	}

	/**
	 * Decode value.
	 * @param value the value requires to decode
	 * @return the decoded value
	 */
	public static String decode(String value) {
		if (value == null) {
			return value;
		}
		String decodedValue = value;
		try {
			decodedValue = URLDecoder.decode(value, UTF_8);
		}
		catch (UnsupportedEncodingException ex) {
		}
		return decodedValue;
	}

	/**
	 * encode value.
	 * @param value the value requires to encode
	 * @return the encoded value
	 */
	public static String encode(String value) {
		String encodedValue = value;
		try {
			encodedValue = URLEncoder.encode(value, UTF_8);
		}
		catch (UnsupportedEncodingException ex) {
		}
		return encodedValue;
	}

	private static void addParam(MultiValueMap<String, String> paramsMap, String name,
			String value) {
		String paramValue = trimAllWhitespace(value);
		if (!StringUtils.hasText(paramValue)) {
			paramValue = EMPTY_VALUE;
		}
		paramsMap.add(trimAllWhitespace(name), paramValue);
	}

}
