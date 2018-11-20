package org.springframework.cloud.stream.binder.rocketmq.consuming;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class Acknowledgement {

	/**
	 * for {@link ConsumeConcurrentlyContext} using
	 */
	private ConsumeConcurrentlyStatus consumeConcurrentlyStatus = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	/**
	 * Message consume retry strategy<br>
	 * -1,no retry,put into DLQ directly<br>
	 * 0,broker control retry frequency<br>
	 * >0,client control retry frequency
	 */
	private Integer consumeConcurrentlyDelayLevel = 0;

	/**
	 * for {@link ConsumeOrderlyContext} using
	 */
	private ConsumeOrderlyStatus consumeOrderlyStatus = ConsumeOrderlyStatus.SUCCESS;
	private Long consumeOrderlySuspendCurrentQueueTimeMill = -1L;

	public Acknowledgement setConsumeConcurrentlyStatus(
			ConsumeConcurrentlyStatus consumeConcurrentlyStatus) {
		this.consumeConcurrentlyStatus = consumeConcurrentlyStatus;
		return this;
	}

	public ConsumeConcurrentlyStatus getConsumeConcurrentlyStatus() {
		return consumeConcurrentlyStatus;
	}

	public ConsumeOrderlyStatus getConsumeOrderlyStatus() {
		return consumeOrderlyStatus;
	}

	public Acknowledgement setConsumeOrderlyStatus(
			ConsumeOrderlyStatus consumeOrderlyStatus) {
		this.consumeOrderlyStatus = consumeOrderlyStatus;
		return this;
	}

	public Integer getConsumeConcurrentlyDelayLevel() {
		return consumeConcurrentlyDelayLevel;
	}

	public void setConsumeConcurrentlyDelayLevel(Integer consumeConcurrentlyDelayLevel) {
		this.consumeConcurrentlyDelayLevel = consumeConcurrentlyDelayLevel;
	}

	public Long getConsumeOrderlySuspendCurrentQueueTimeMill() {
		return consumeOrderlySuspendCurrentQueueTimeMill;
	}

	public void setConsumeOrderlySuspendCurrentQueueTimeMill(
			Long consumeOrderlySuspendCurrentQueueTimeMill) {
		this.consumeOrderlySuspendCurrentQueueTimeMill = consumeOrderlySuspendCurrentQueueTimeMill;
	}

	public static Acknowledgement buildOrderlyInstance() {
		Acknowledgement acknowledgement = new Acknowledgement();
		acknowledgement.setConsumeOrderlyStatus(ConsumeOrderlyStatus.SUCCESS);
		return acknowledgement;
	}

	public static Acknowledgement buildConcurrentlyInstance() {
		Acknowledgement acknowledgement = new Acknowledgement();
		acknowledgement
				.setConsumeConcurrentlyStatus(ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
		return acknowledgement;
	}

}
