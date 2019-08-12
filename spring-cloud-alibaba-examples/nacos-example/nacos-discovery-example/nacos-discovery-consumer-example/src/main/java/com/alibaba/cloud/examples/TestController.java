package com.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

	@RequestMapping(value = "/echo-rest/{str}", method = RequestMethod.GET)
	public String rest(@PathVariable String str) {
		return restTemplate.getForObject("http://service-provider/echo/" + str,
				String.class);
	}

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() {
		return restTemplate1.getForObject("http://service-provider", String.class);
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test() {
		return restTemplate1.getForObject("http://service-provider/test", String.class);
	}

	@RequestMapping(value = "/sleep", method = RequestMethod.GET)
	public String sleep() {
		return restTemplate1.getForObject("http://service-provider/sleep", String.class);
	}

	@RequestMapping(value = "/notFound-feign", method = RequestMethod.GET)
	public String notFound() {
		return echoService.notFound();
	}

	@RequestMapping(value = "/divide-feign", method = RequestMethod.GET)
	public String divide(@RequestParam Integer a, @RequestParam Integer b) {
		return echoService.divide(a, b);
	}

    @RequestMapping(value = "/divide-feign2", method = RequestMethod.GET)
    public String divide(@RequestParam Integer a) {
        return echoService.divide(a);
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
