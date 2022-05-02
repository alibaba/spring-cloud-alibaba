package com.alibaba.cloud.integration.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@Slf4j
public abstract class RocketmqSourceTester<ServiceContainerT extends GenericContainer> {

	public static final String INSERT = "INSERT";

	public static final String DELETE = "DELETE";

	public static final String UPDATE = "UPDATE";

	protected final String sourceType;
	protected final Map<String, Object> sourceConfig;

	protected int numEntriesToInsert = 1;
	protected int numEntriesExpectAfterStart = 9;

	public static final Set<String> DEBEZIUM_FIELD_SET = new HashSet<String>() {{
		add("before");
		add("after");
		add("source");
		add("op");
		add("ts_ms");
		add("transaction");
	}};

	protected RocketmqSourceTester(String sourceType) {
		this.sourceType = sourceType;
		this.sourceConfig = Maps.newHashMap();
	}

	public abstract void setServiceContainer(ServiceContainerT serviceContainer);

	public String sourceType() {
		return sourceType;
	}

	public Map<String, Object> sourceConfig() {
		return sourceConfig;
	}

	public abstract void prepareSource() throws Exception;

	public abstract void prepareInsertEvent() throws Exception;

	public abstract void prepareDeleteEvent() throws Exception;

	public abstract void prepareUpdateEvent() throws Exception;

	public abstract Map<String, String> produceSourceMessages(int numMessages) throws Exception;

	public void validateSourceResult(Consumer consumer, int number,
									 String eventType, String converterClassName) throws Exception {
		doPreValidationCheck(eventType);
		if (converterClassName.endsWith("AvroConverter")) {
			validateSourceResultAvro(consumer, number, eventType);
		} else {
			validateSourceResultJson(consumer, number, eventType);
		}
		doPostValidationCheck(eventType);
	}

	/**
	 * Execute before regular validation to check database-specific state.
	 */
	public void doPreValidationCheck(String eventType) {
		log.info("pre-validation of {}", eventType);
	}

	/**
	 * Execute after regular validation to check database-specific state.
	 */
	public void doPostValidationCheck(String eventType) {
		log.info("post-validation of {}", eventType);
	}

	public void validateSourceResultJson(Consumer<KeyValue<byte[], byte[]>> consumer, int number, String eventType) throws Exception {
		int recordsNumber = 0;
//		Message<KeyValue<byte[], byte[]>> msg = consumer.receive(initialDelayForMsgReceive(), TimeUnit.SECONDS);
//		while(msg != null) {
//			recordsNumber ++;
//			final String key = new String(msg.getValue().getKey());
//			final String value = new String(msg.getValue().getValue());
//			log.info("Received message: key = {}, value = {}.", key, value);
//			Assert.assertTrue(key.contains(this.keyContains()));
//			Assert.assertTrue(value.contains(this.valueContains()));
//			if (eventType != null) {
//				Assert.assertTrue(value.contains(this.eventContains(eventType, true)));
//			}
//			consumer.acknowledge(msg);
//			msg = consumer.receive(1, TimeUnit.SECONDS);
//		}
//
//		Assert.assertEquals(recordsNumber, number);
//		log.info("Stop {} server container. topic: {} has {} records.", getSourceType(), consumer.getTopic(), recordsNumber);
	}

	public <K,V>  void validateSourceResultAvro(Consumer<KeyValue<K, V>> consumer,
										 int number, String eventType) throws Exception {
		int recordsNumber = 0;
//		Message<KeyValue<K,V>> msg = consumer.receive(initialDelayForMsgReceive(), TimeUnit.SECONDS);
//		while(msg != null) {
//			recordsNumber ++;
//			K keyRecord = msg.getValue().getKey();
//			Assert.assertNotNull(keyRecord.getFields());
//			Assert.assertTrue(keyRecord.getFields().size() > 0);
//
//			V valueRecord = msg.getValue().getValue();
//			Assert.assertNotNull(valueRecord.getFields());
//			Assert.assertTrue(valueRecord.getFields().size() > 0);
//
//			log.info("Received message: key = {}, value = {}.", keyRecord.getNativeObject(), valueRecord.getNativeObject());
//			for (Field field : valueRecord.getFields()) {
//				log.info("validating field {}", field.getName());
//				Assert.assertTrue(DEBEZIUM_FIELD_SET.contains(field.getName()));
//			}
//
//			if (eventType != null) {
//				String op = valueRecord.getField("op").toString();
//				Assert.assertEquals(this.eventContains(eventType, false), op);
//			}
//			consumer.acknowledge(msg);
//			msg = consumer.receive(1, TimeUnit.SECONDS);
//		}
//
//		Assert.assertEquals(recordsNumber, number);
//		log.info("Stop {} server container. topic: {} has {} records.", getSourceType(), consumer.getTopic(), recordsNumber);
	}

	public int initialDelayForMsgReceive() {
		return 2;
	}

	public String keyContains() {
		return "dbserver1.inventory.products.Key";
	}

	public String valueContains() {
		return "dbserver1.inventory.products.Value";
	}

	public String eventContains(String eventType, boolean isJson) {
		if (eventType.equals(INSERT)) {
			return isJson ? "\"op\":\"c\"" : "c";
		} else if (eventType.equals(UPDATE)) {
			return isJson ? "\"op\":\"u\"" : "u";
		} else {
			return isJson ? "\"op\":\"d\"" : "d";
		}
	}
}
