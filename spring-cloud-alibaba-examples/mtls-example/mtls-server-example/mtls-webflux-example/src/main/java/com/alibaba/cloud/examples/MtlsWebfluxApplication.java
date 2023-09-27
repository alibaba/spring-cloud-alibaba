package com.alibaba.cloud.examples;

import java.security.cert.X509Certificate;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@SpringBootApplication
public class MtlsWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtlsWebfluxApplication.class, args);
	}

	@RestController
	public class WebFluxController {

		@GetMapping("/webflux/get")
		public Mono<String> get(ServerWebExchange exchange) {
			X509Certificate[] certs = exchange.getRequest().getSslInfo().getPeerCertificates();
			if(certs != null){
				for(int i=0;i<certs.length;i++){
					System.out.println("client certificate_"+i+": \n" + certs[i].toString());
				}
			}
			return Mono.just("webflux-server received request from client");
		}
	}
}
