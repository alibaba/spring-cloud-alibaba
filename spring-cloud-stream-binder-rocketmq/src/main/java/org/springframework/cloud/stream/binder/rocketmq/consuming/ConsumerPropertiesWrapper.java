package org.springframework.cloud.stream.binder.rocketmq.consuming;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
class ConsumerPropertiesWrapper {
    private final String tags;
    private final String group;
    private final String topic;
    private final Set<String> tagsSet;

    ConsumerPropertiesWrapper(String group, String topic, String tags) {
        this.tags = tags;
        this.group = group;
        this.topic = topic;
        tagsSet = tags == null ? new HashSet<>() : Arrays.stream(tags.split("\\|\\|")).map(String::trim).collect(
            Collectors.toSet());
    }

    String getTags() {
        return tags;
    }

    String getGroup() {
        return group;
    }

    String getTopic() {
        return topic;
    }

    Set<String> getTagsSet() {
        return tagsSet;
    }

}
