package com.alibaba.cloud.ai.tongyi.exception;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiAPIClientException extends RuntimeException{

	public TongYiAPIClientException(String message) {

		super(message);
	}

	public TongYiAPIClientException(String message, Throwable cause) {

		super(message, cause);
	}
}
