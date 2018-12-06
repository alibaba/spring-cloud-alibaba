package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.cloud.examples.ConsumerApplication.EchoService;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */
@RestController
public class TestController {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private EchoService echoService;

	@Autowired
	private DiscoveryClient discoveryClient;

	@RequestMapping(value = "/echo-rest/{str}", method = RequestMethod.GET)
	public String rest(@PathVariable String str) {
		return restTemplate.getForObject("http://service-provider/echo/" + str,
				String.class);
	}

	@RequestMapping(value = "/notFound-feign", method = RequestMethod.GET)
	public String notFound() {
		return echoService.notFound();
	}

	@RequestMapping(value = "/divide-feign", method = RequestMethod.GET)
	public String divide(@RequestParam Integer a, @RequestParam Integer b) {
		return echoService.divide(a, b);
	}

	@RequestMapping(value = "/echo-feign/{str}", method = RequestMethod.GET)
	public String feign(@PathVariable String str) {
		return echoService.echo(str);
	}

	@RequestMapping(value = "/services/{service}", method = RequestMethod.GET)
	public Object client(@PathVariable String service) {
		return discoveryClient.getInstances(service);
	}

	@RequestMapping(value = "/services", method = RequestMethod.GET)
	public Object services() {
		return discoveryClient.getServices();
	}
}
