package org.springframework.cloud.alibaba.cloud.examples;

import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.message.Message;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class MyTransactionExecuter implements LocalTransactionExecuter {
	@Override
	public LocalTransactionState executeLocalTransactionBranch(Message msg, Object arg) {
		if ("1".equals(msg.getUserProperty("test"))) {
			System.out.println(new String(msg.getBody()) + " rollback");
			return LocalTransactionState.ROLLBACK_MESSAGE;
		}
		System.out.println(new String(msg.getBody()) + " commit");
		return LocalTransactionState.COMMIT_MESSAGE;
	}
}
