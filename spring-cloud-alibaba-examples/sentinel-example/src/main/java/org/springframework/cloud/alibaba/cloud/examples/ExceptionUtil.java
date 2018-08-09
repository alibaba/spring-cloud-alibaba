package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author fangjian
 */
public class ExceptionUtil {

	public static void handleException(BlockException ex) {
		System.out.println("Oops: " + ex.getClass().getCanonicalName());
	}

}
