package com.alibaba.cloud.integration.test.nacos.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;

public class HttpClient4Test {
		
		protected URL baseURL;
		
		@Autowired protected TestRestTemplate restTemplate;
		
		protected <T> ResponseEntity<T> request(String path,
				MultiValueMap<String, String> params, Class<T> clazz) {
				
				HttpHeaders headers = new HttpHeaders();
				
				HttpEntity<?> entity = new HttpEntity<T>(headers);
				
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
						this.baseURL.toString() + path).queryParams(params);
				
				return this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
						entity, clazz);
		}
		
		protected <T> ResponseEntity<T> request(String path,
				MultiValueMap<String, String> params, Class<T> clazz,
				HttpMethod httpMethod) {
				
				HttpHeaders headers = new HttpHeaders();
				
				HttpEntity<?> entity = new HttpEntity<T>(headers);
				
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
						this.baseURL.toString() + path).queryParams(params);
				
				return this.restTemplate.exchange(builder.toUriString(), httpMethod,
						entity, clazz);
		}
		
}