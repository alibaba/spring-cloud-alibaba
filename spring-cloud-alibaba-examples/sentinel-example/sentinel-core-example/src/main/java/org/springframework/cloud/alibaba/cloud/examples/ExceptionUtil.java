package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.cloud.alibaba.sentinel.rest.SentinelClientHttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author fangjian
 */
public class ExceptionUtil {

	public static SentinelClientHttpResponse handleException(HttpRequest request,
			byte[] body, ClientHttpRequestExecution execution, BlockException ex) {
		System.out.println("Oops: " + ex.getClass().getCanonicalName());
		return new SentinelClientHttpResponse("custom block info");
	}

}
