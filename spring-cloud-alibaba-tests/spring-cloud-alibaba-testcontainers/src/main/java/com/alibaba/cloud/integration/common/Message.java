/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.cloud.integration.common;

import java.util.Map;
import java.util.Optional;

/**
 * message abstraction .
 */

public interface Message<T> {

    /**
     * Return the properties attached to the message.
     *
     * <p>Properties are application defined key/value pairs that will be attached to the message.
     *
     * @return an unmodifiable view of the properties map
     */
    Map<String, String> getProperties();

    /**
     * Check whether the message has a specific property attached.
     *
     * @param name the name of the property to check
     * @return true if the message has the specified property and false if the properties is not defined
     */
    boolean hasProperty(String name);

    /**
     * Get the value of a specific property.
     *
     * @param name the name of the property
     * @return the value of the property or null if the property was not defined
     */
    String getProperty(String name);

    /**
     * Get the raw payload of the message.
     *
     * <p>Even when using the Schema and type-safe API, an application
     * has access to the underlying raw message payload.
     *
     * @return the byte array with the message payload
     */
    byte[] getData();

    /**
     * Get the uncompressed message payload size in bytes.
     * 
     * @return size in bytes. 
     */
    int size();

    /**
     * Get the de-serialized value of the message, according the configured {@link Schema}.
     *
     * @return the deserialized value of the message
     */
    T getValue();
    
    /**
     * Get the publish time of this message. The publish time is the timestamp that a client publish the message.
     *
     * @return publish time of this message.
     * @see #getEventTime()
     */
    long getPublishTime();
    

    /**
     * Get the producer name who produced this message.
     *
     * @return producer name who produced this message, null if producer name is not set.
     * @since 1.22.0
     */
    String getProducerName();

    /**
     * Check whether the message has a key.
     *
     * @return true if the key was set while creating the message and false if the key was not set
     * while creating the message
     */
    boolean hasKey();

    /**
     * Get the key of the message.
     *
     * @return the key of the message
     */
    String getKey();

    /**
     * Check whether the key has been base64 encoded.
     *
     * @return true if the key is base64 encoded, false otherwise
     */
    boolean hasBase64EncodedKey();

    /**
     * Get bytes in key. If the key has been base64 encoded, it is decoded before being returned.
     * Otherwise, if the key is a plain string, this method returns the UTF_8 encoded bytes of the string.
     * @return the key in byte[] form
     */
    byte[] getKeyBytes();

    /**
     * Check whether the message has a ordering key.
     *
     * @return true if the ordering key was set while creating the message
     *         false if the ordering key was not set while creating the message
     */
    boolean hasOrderingKey();

    /**
     * Get the ordering key of the message.
     *
     * @return the ordering key of the message
     */
    byte[] getOrderingKey();

    /**
     * Get the topic the message was published to.
     *
     * @return the topic the message was published to
     */
    String getTopicName();
    

    /**
     * Get message redelivery count, redelivery count maintain in  broker. When client acknowledge message
     * timeout, broker will dispatch message again with message redelivery count in CommandMessage defined.
     *
     * <p>Message redelivery increases monotonically in a broker, when topic switch ownership to a another broker
     * redelivery count will be recalculated.
     *
     * @since 2.3.0
     * @return message redelivery count
     */
    int getRedeliveryCount();

    /**
     * Get schema version of the message.
     * @since 2.4.0
     * @return Schema version of the message if the message is produced with schema otherwise null.
     */
    byte[] getSchemaVersion();

    /**
     * Get the schema associated to the message.
     * Please note that this schema is usually equal to the Schema you passed
     * during the construction of the Consumer or the Reader.
     * But if you are consuming the topic using the GenericObject interface
     * this method will return the schema associated with the message.
     * @return The schema used to decode the payload of message.
     * @see Schema#AUTO_CONSUME()
     */
    default Optional<Schema<?>> getReaderSchema() {
        return Optional.empty();
    }

    /**
     * Check whether the message is replicated from other cluster.
     *
     * @since 2.4.0
     * @return true if the message is replicated from other cluster.
     *         false otherwise.
     */
    boolean isReplicated();

    /**
     * Get name of cluster, from which the message is replicated.
     *
     * @since 2.4.0
     * @return the name of cluster, from which the message is replicated.
     */
    String getReplicatedFrom();

    /**
     * Release a message back to the pool. This is required only if the consumer was created with the option to pool
     * messages, otherwise it will have no effect.
     * 
     * @since 2.8.0
     */
    void release();

    /**
     * Check whether the message has a broker publish time
     *
     * @since 2.9.0
     * @return true if the message has a broker publish time, otherwise false.
     */
    boolean hasBrokerPublishTime();

    /**
     * Get broker publish time from broker entry metadata.
     * Note that only if the feature is enabled in the broker then the value is available.
     *
     * @since 2.9.0
     * @return broker publish time from broker entry metadata, or empty if the feature is not enabled in the broker.
     */
    Optional<Long> getBrokerPublishTime();

    /**
     * Check whether the message has a index.
     *
     * @since 2.9.0
     * @return true if the message has a index, otherwise false.
     */
    boolean hasIndex();

    /**
     * Get index from broker entry metadata.
     * Note that only if the feature is enabled in the broker then the value is available.
     *
     * @since 2.9.0
     * @return index from broker entry metadata, or empty if the feature is not enabled in the broker.
     */
    Optional<Long> getIndex();
}
