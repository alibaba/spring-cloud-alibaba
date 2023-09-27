package com.alibaba.cloud.examples;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.governance.istio.sds.CertUpdateCallback;
import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import org.apache.http.impl.client.HttpClientBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ResttemplateTestController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AbstractCertManager abstractCertManager;

	@Autowired
	private MtlsClientSSLContext mtlsSSLContext;

	@GetMapping("/resttemplate/getMvc")
	public String getMvc(HttpServletRequest httpServletRequest) {
		X509Certificate[] certs = (X509Certificate[]) httpServletRequest.getAttribute("javax.servlet.request.X509Certificate");
		return restTemplate.getForObject("https://mtls-mvc-example/mvc/get", String.class);
	}

	@GetMapping("/resttemplate/getWebflux")
	public String getWebflux(HttpServletRequest httpServletRequest) {
		X509Certificate[] certs = (X509Certificate[]) httpServletRequest.getAttribute("javax.servlet.request.X509Certificate");
		return restTemplate.getForObject("https://mtls-webflux-example/webflux/get", String.class);
	}

	@GetMapping("/checkpreload")
	public String checkpreload() {
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().setSSLContext(mtlsSSLContext.getSslContext()).setSSLHostnameVerifier(mtlsSSLContext.getHostnameVerifier()).build()));
		return "success";
	}

	@RequestMapping("/reload")
	public String reload() throws IOException, NoSuchFieldException, IllegalAccessException {
		CertPair newCert = new CertPair();
		newCert.setExpireTime(Long.MAX_VALUE);
		newCert.setPrivateKey(KeyStoreUtil.loadPrivateKey(FileUtils.readFileToString(new File(
						"./cert/dname.key"),
				StandardCharsets.UTF_8), "EC"));
		List<String> list = new ArrayList<>();
		list.add(FileUtils.readFileToString(
				new File(
						"./cert/dname.crt"),
				StandardCharsets.UTF_8));
		list.add(FileUtils.readFileToString(
				new File(
						"./cert/dname.crt"),
				StandardCharsets.UTF_8));

		newCert.setCertificateChain(list);
		Field field = AbstractCertManager.class.getDeclaredField("certPair");
		field.setAccessible(true);
		field.set(abstractCertManager, newCert);
		Field callbacks = AbstractCertManager.class.getDeclaredField("callbacks");
		callbacks.setAccessible(true);
		List<CertUpdateCallback> callbackList = (List<CertUpdateCallback>) callbacks.get(abstractCertManager);
		for (CertUpdateCallback callback : callbackList) {
			callback.onUpdateCert(newCert);
		}
		return "success";
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}