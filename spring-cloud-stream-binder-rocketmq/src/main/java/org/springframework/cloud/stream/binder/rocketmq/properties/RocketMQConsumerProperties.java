package org.springframework.cloud.stream.binder.rocketmq.properties;

import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQConsumerProperties {

    /**
     * using '||' to split tag
     * {@link MQPushConsumer#subscribe(String, String)}
     */
    private String tags;

    /**
	 * {@link MQPushConsumer#subscribe(String, MessageSelector)}
	 * {@link MessageSelector#bySql(String)}
	 */
    private String sql;

    /**
     * {@link MessageModel#BROADCASTING}
     */
    private Boolean broadcasting = false;

    /**
     * if orderly is true, using {@link MessageListenerOrderly}
     * else if orderly if false, using {@link MessageListenerConcurrently}
     */
    private Boolean orderly = false;

    private Boolean enabled = true;

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Boolean getOrderly() {
        return orderly;
    }

    public void setOrderly(Boolean orderly) {
        this.orderly = orderly;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getBroadcasting() {
        return broadcasting;
    }

    public void setBroadcasting(Boolean broadcasting) {
        this.broadcasting = broadcasting;
    }
}
