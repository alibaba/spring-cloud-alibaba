package com.alibaba.cloud.governance.common.matcher;

import org.springframework.http.HttpHeaders;

import java.util.List;

public class HeaderMatcher {

	private StringMatcher stringMatcher;

	public boolean match(HttpHeaders headers, String headerName) {
		List<String> headerValues = headers.getValuesAsList(headerName);
		for (String headerValue : headerValues) {
			if (stringMatcher.match(headerValue)) {
				return true;
			}
		}
		return false;
	}

	public HeaderMatcher(StringMatcher stringMatcher) {
		this.stringMatcher = stringMatcher;
	}

}
