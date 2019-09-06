package com.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.alibaba.cloud.examples.ConsumerApplication.EchoService;

/**
 * @author xiaojing
 */
@RestController
public class TestController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplate restTemplate1;

	@Autowired
	private EchoService echoService;

	@Autowired
	private DiscoveryClient discoveryClient;

	// @PostConstruct
	// public void init() {
	// restTemplate1.setErrorHandler(new ResponseErrorHandler() {
	// @Override
	// public boolean hasError(ClientHttpResponse response) throws IOException {
	// return false;
	// }
	//
	// @Override
	// public void handleError(ClientHttpResponse response) throws IOException {
	// System.err.println("handle error");
	// }
	// });
	// }

	@GetMapping(value = "/echo-rest/{str}")
	public String rest(@PathVariable String str) {
		return restTemplate.getForObject("http://service-provider/echo/" + str,
				String.class);
	}

	@GetMapping(value = "/index")
	public String index() {
		return restTemplate1.getForObject("http://service-provider", String.class);
	}

	@GetMapping(value = "/test")
	public String test() {
		return restTemplate1.getForObject("http://service-provider/test", String.class);
	}

	@GetMapping(value = "/sleep")
	public String sleep() {
		return restTemplate1.getForObject("http://service-provider/sleep", String.class);
	}

	@GetMapping(value = "/notFound-feign")
	public String notFound() {
		return echoService.notFound();
	}

	@GetMapping(value = "/divide-feign")
	public String divide(@RequestParam Integer a, @RequestParam Integer b) {
		return echoService.divide(a, b);
	}

	@GetMapping(value = "/divide-feign2")
	public String divide(@RequestParam Integer a) {
		return echoService.divide(a);
	}

	@GetMapping(value = "/echo-feign/{str}")
	public String feign(@PathVariable String str) {
		return echoService.echo(str);
	}

	@GetMapping(value = "/services/{service}")
	public Object client(@PathVariable String service) {
		return discoveryClient.getInstances(service);
	}

	@GetMapping(value = "/services")
	public Object services() {
		return discoveryClient.getServices();
	}
}
