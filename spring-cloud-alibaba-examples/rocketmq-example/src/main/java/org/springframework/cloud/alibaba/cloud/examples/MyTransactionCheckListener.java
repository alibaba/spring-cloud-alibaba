package org.springframework.cloud.alibaba.cloud.examples;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class MyTransactionCheckListener implements TransactionCheckListener {

	@Override
	public LocalTransactionState checkLocalTransactionState(MessageExt msg) {
		System.out.println("TransactionCheckListener: " + new String(msg.getBody()));
		return LocalTransactionState.COMMIT_MESSAGE;
	}

}
