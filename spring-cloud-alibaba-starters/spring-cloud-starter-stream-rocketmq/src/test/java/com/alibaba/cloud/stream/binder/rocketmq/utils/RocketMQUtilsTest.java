/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.stream.binder.rocketmq.utils;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RocketMQUtils}.
 */
public class RocketMQUtilsTest {

	@Test
	public void getInstanceNameWithoutSharingProducesDistinctNames() {
		String first = RocketMQUtils.getInstanceName(null, "group-a");
		String second = RocketMQUtils.getInstanceName(null, "group-a");
		assertThat(first).isNotEqualTo(second);
	}

	@Test
	public void getInstanceNameWithSharingProducesDeterministicNameForSameIdentify() {
		String first = RocketMQUtils.getInstanceName(null, "group-a", true);
		String second = RocketMQUtils.getInstanceName(null, "group-a", true);
		assertThat(first).isEqualTo(second);
	}

	@Test
	public void getInstanceNameWithSharingStillDifferentiatesByIdentify() {
		String first = RocketMQUtils.getInstanceName(null, "group-a", true);
		String second = RocketMQUtils.getInstanceName(null, "group-b", true);
		assertThat(first).isNotEqualTo(second);
	}

	@Test
	public void getInstanceNameWithSharingOmitsNanoTimeSuffix() {
		String name = RocketMQUtils.getInstanceName(null, "group-a", true);
		// Shared name format is "identify|pid" — exactly one separator, two segments.
		assertThat(name.split("\\|")).hasSize(2);
	}

	@Test
	public void getInstanceNameWithoutSharingIncludesNanoTimeSuffix() {
		String name = RocketMQUtils.getInstanceName(null, "group-a", false);
		// Unshared name format is "identify|pid|nanoTime" — three segments.
		assertThat(name.split("\\|")).hasSize(3);
	}

	@Test
	public void getInstanceNameWithRpcHookPrependsAccessKey() {
		AclClientRPCHook hook = new AclClientRPCHook(
				new SessionCredentials("ak-1", "sk-1"));
		String name = RocketMQUtils.getInstanceName(hook, "group-a", true);
		// "ak|identify|pid" when shared.
		assertThat(name).startsWith("ak-1|group-a|");
		assertThat(name.split("\\|")).hasSize(3);
	}

	@Test
	public void defaultOverloadDelegatesToUnsharedBehavior() {
		String defaultName = RocketMQUtils.getInstanceName(null, "group-a");
		String unsharedName = RocketMQUtils.getInstanceName(null, "group-a", false);
		assertThat(defaultName.split("\\|")).hasSize(3);
		assertThat(unsharedName.split("\\|")).hasSize(3);
	}

	@Test
	public void mergeRocketMQPropertiesPropagatesShareClientInstanceFromBinder() {
		RocketMQBinderConfigurationProperties binder = new RocketMQBinderConfigurationProperties();
		binder.setShareClientInstance(true);
		RocketMQConsumerProperties consumer = new RocketMQConsumerProperties();
		assertThat(consumer.isShareClientInstance()).isFalse();
		RocketMQUtils.mergeRocketMQProperties(binder, consumer);
		assertThat(consumer.isShareClientInstance()).isTrue();
	}

	@Test
	public void mergeRocketMQPropertiesPreservesConsumerShareClientInstanceWhenBinderIsFalse() {
		RocketMQBinderConfigurationProperties binder = new RocketMQBinderConfigurationProperties();
		RocketMQConsumerProperties consumer = new RocketMQConsumerProperties();
		consumer.setShareClientInstance(true);
		RocketMQUtils.mergeRocketMQProperties(binder, consumer);
		assertThat(consumer.isShareClientInstance()).isTrue();
	}

	@Test
	public void mergeRocketMQPropertiesDefaultLeavesShareClientInstanceFalse() {
		RocketMQBinderConfigurationProperties binder = new RocketMQBinderConfigurationProperties();
		RocketMQConsumerProperties consumer = new RocketMQConsumerProperties();
		RocketMQUtils.mergeRocketMQProperties(binder, consumer);
		assertThat(consumer.isShareClientInstance()).isFalse();
	}

}
