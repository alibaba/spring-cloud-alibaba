package com.alibaba.cloud.examples;

public class UrlCleaner {
	public static String clean(String url) {
		System.out.println("enter urlCleaner");
		if (url.matches(".*/echo/.*")) {
			System.out.println("change url");
			url = url.replaceAll("/echo/.*", "/echo/{str}");
		}
		return url;
	}
}
